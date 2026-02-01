package me.herex.farepixelduels.quests;

public enum QuestType {
    WIN_GAMES,
    PLAY_GAMES,
    GET_KILLS;

    public static QuestType fromString(String s) {
        if (s == null) return WIN_GAMES;
        try {
            return QuestType.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return WIN_GAMES;
        }
    }
}
