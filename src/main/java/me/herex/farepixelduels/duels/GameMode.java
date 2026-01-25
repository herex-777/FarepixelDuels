package me.herex.farepixelduels.duels;

public enum GameMode {
    CLASSIC,
    BOW,
    BOXING,
    OP,
    SUMO,
    UHC,
    NODEBUFF;

    public String getDisplayName() {
        switch (this) {
            case BOW: return "Bow Duel";
            case BOXING: return "Boxing Duel";
            case OP: return "OP Duel";
            case NODEBUFF: return "NoDebuff Duel";
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
