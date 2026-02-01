package me.herex.farepixelduels.quests;

import me.herex.farepixelduels.duels.GameMode;
import org.bukkit.Material;

import java.util.List;

public class QuestDefinition {

    private final String id;
    private final String displayName;
    private final Material icon;
    private final int slot;
    private final QuestType type;
    private final GameMode mode; // null = ANY
    private final int required;
    private final List<String> description;
    private final List<String> rewardCommands;
    private final boolean daily;

    public QuestDefinition(String id,
                           String displayName,
                           Material icon,
                           int slot,
                           QuestType type,
                           GameMode mode,
                           int required,
                           List<String> description,
                           List<String> rewardCommands,
                           boolean daily) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.slot = slot;
        this.type = type;
        this.mode = mode;
        this.required = required;
        this.description = description;
        this.rewardCommands = rewardCommands;
        this.daily = daily;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
    public int getSlot() { return slot; }
    public QuestType getType() { return type; }
    public GameMode getMode() { return mode; }
    public int getRequired() { return required; }
    public List<String> getDescription() { return description; }
    public List<String> getRewardCommands() { return rewardCommands; }
    public boolean isDaily() { return daily; }

    public boolean isGlow() {
        return false;
    }

    public boolean isWeekly() {
        return false;
    }
}
