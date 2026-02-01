package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.quests.QuestDefinition;
import me.herex.farepixelduels.quests.QuestManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class QuestStartListener implements Listener {

    private final QuestManager questManager;

    public QuestStartListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onQuestClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = ChatColor.stripColor(event.getView().getTitle());
        String questsTitle = ChatColor.stripColor(QuestManager.GUI_TITLE);

        if (!title.equalsIgnoreCase(questsTitle)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        QuestDefinition quest = questManager.getQuestFromGui(player, slot);

        if (quest == null) return;

        // Start the quest
        questManager.handleQuestStart(player, quest);

        // Make item glow
        ItemStack item = event.getCurrentItem();
        if (item != null && item.getType() != Material.AIR) {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Started: " + quest.getDisplayName());
            item.setItemMeta(meta);
        }

        // Play sound
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);

        // Send confirmation message
        player.sendMessage(ChatColor.GREEN + "You have started the quest: " + quest.getDisplayName());

        // Update the quest item in the inventory
        questManager.openQuestsGui(player);
    }
}
