package me.herex.farepixelduels.duels;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class TitleUtil {

    private TitleUtil() {}

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        try {
            Object enumTitle = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
            Object enumSub = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null);

            Object chatTitle = getChatComponent(title);
            Object chatSub = getChatComponent(subtitle);

            Constructor<?> cons = getNMSClass("PacketPlayOutTitle").getConstructor(
                    getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
                    getNMSClass("IChatBaseComponent"),
                    int.class, int.class, int.class
            );

            Object packetTitle = cons.newInstance(enumTitle, chatTitle, fadeIn, stay, fadeOut);
            Object packetSub = cons.newInstance(enumSub, chatSub, fadeIn, stay, fadeOut);

            sendPacket(player, packetTitle);
            sendPacket(player, packetSub);
        } catch (Throwable ignored) {
        }
    }

    private static Object getChatComponent(String text) throws Exception {
        Class<?> chatComp = getNMSClass("IChatBaseComponent");
        Class<?> chatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
        Method m = chatSerializer.getMethod("a", String.class);
        return m.invoke(null, "{\"text\":\"" + escape(text) + "\"}");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }


    private static void sendPacket(Player player, Object packet) throws Exception {
        Object handle = player.getClass().getMethod("getHandle").invoke(player);
        Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
        Method sendPacket = playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
        sendPacket.invoke(playerConnection, packet);
    }

    private static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("net.minecraft.server." + version + "." + name);
    }
}
