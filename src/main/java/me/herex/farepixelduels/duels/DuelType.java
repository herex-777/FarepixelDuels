package me.herex.farepixelduels.duels;

public enum DuelType {
    ONE_VS_ONE(2),
    TWO_VS_TWO(4);

    private final int size;

    DuelType(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public static DuelType fromString(String s) {
        if (s == null) return ONE_VS_ONE;
        s = s.trim().toLowerCase().replace(" ", "");
        if (s.equals("2v2") || s.equals("twovstwo")) return TWO_VS_TWO;
        return ONE_VS_ONE;
    }

    public String display() {
        return this == TWO_VS_TWO ? "2v2" : "1v1";
    }
}
