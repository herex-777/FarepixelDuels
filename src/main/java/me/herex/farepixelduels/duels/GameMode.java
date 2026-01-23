package me.herex.farepixelduels.duels;

public enum GameMode {
    CLASSIC,
    BOW,
    COMBO,
    BOXING,
    OP,
    SUMO,
    UHC;

    public String getDisplayName() {
        switch (this) {
            case BOW: return "Bow Duel";
            case COMBO: return "Combo Duel";
            case BOXING: return "Boxing Duel";
            case OP: return "OP Duel";
            case SUMO: return "Sumo Duel";
            case UHC: return "UHC Duel";
            default: return "Classic Duel";
        }
    }

    public static GameMode fromString(String s) {
        if (s == null) return null;
        try {
            return GameMode.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public String id() {
        return name().toLowerCase();
    }
}
