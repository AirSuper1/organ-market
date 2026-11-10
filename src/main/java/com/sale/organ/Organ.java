package com.sale.organ;

public enum Organ {
    STOMACH("stomach", "Желудок", 16, 40),
    KIDNEY("kidney", "Почка", 12, 30),
    LUNG("lung", "Лёгкое", 20, 50),
    EYE("eye", "Глаз", 25, 64);

    private final String id;
    private final String displayName;
    private final int reward;
    private final int buybackCost;

    Organ(String id, String displayName, int reward, int buybackCost) {
        this.id = id;
        this.displayName = displayName;
        this.reward = reward;
        this.buybackCost = buybackCost;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getReward() { return reward; }
    public int getBuybackCost() { return buybackCost; }

    public static Organ fromId(String id) {
        for (Organ organ : values()) {
            if (organ.id.equals(id)) {
                return organ;
            }
        }
        return null;
    }
}