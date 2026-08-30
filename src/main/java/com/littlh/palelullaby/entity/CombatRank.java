package com.littlh.palelullaby.entity;

/**
 * 吸血鬼三阶数值（无铁魔法路径）。
 * 施法间隔以 tick 计：8~14 秒 / 6~12 秒 / 5~10 秒。
 */
public enum CombatRank {
    RANK_1(26, 5, 1.15F, 2, 0, 0.0F, 160, 280),
    RANK_2(48, 8, 1.20F, 6, 0, 0.0F, 120, 240),
    RANK_3(80, 12, 1.25F, 12, 4, 0.4F, 100, 200);

    private final double maxHealth;
    private final double attackDamage;
    private final float movementSpeed;
    private final double armor;
    private final double toughness;
    private final double knockbackResistance;
    private final int castIntervalMin;
    private final int castIntervalMax;

    CombatRank(double maxHealth, double attackDamage, float movementSpeed,
               double armor, double toughness, double knockbackResistance,
               int castIntervalMin, int castIntervalMax) {
        this.maxHealth = maxHealth;
        this.attackDamage = attackDamage;
        this.movementSpeed = movementSpeed;
        this.armor = armor;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.castIntervalMin = castIntervalMin;
        this.castIntervalMax = castIntervalMax;
    }

    public double maxHealth() { return maxHealth; }
    public double attackDamage() { return attackDamage; }
    public float movementSpeed() { return movementSpeed; }
    public double armor() { return armor; }
    public double toughness() { return toughness; }
    public double knockbackResistance() { return knockbackResistance; }
    public int castIntervalMin() { return castIntervalMin; }
    public int castIntervalMax() { return castIntervalMax; }
}
