package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.quests.QuestDefinition;
import me.herex.farepixelduels.quests.QuestManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class QuestGUIListener implements Listener {

    private final QuestManager questManager;

    public QuestGUIListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();
        String title = ChatColor.stripColor(e.getView().getTitle());
        String questsTitle = ChatColor.stripColor(QuestManager.GUI_TITLE);

        if (!title.equalsIgnoreCase(questsTitle)) return;

        if (e.getRawSlot() >= e.getView().getTopInventory().getSize()) return;

        e.setCancelled(true);
        e.setResult(org.bukkit.event.Event.Result.DENY);

        QuestDefinition q = questManager.getQuestFromGui(p, e.getRawSlot());
        if (q == null) return;

        questManager.handleQuestClick(p, q);
        // refresh GUI instantly
        questManager.openQuestsGui(p);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();
        String title = ChatColor.stripColor(e.getView().getTitle());
        String questsTitle = ChatColor.stripColor(QuestManager.GUI_TITLE);

        if (title.equalsIgnoreCase(questsTitle)) {
            questManager.clearOpenGui(p);
        }
    }
}
