package com.littlh.palelullaby.entity;

/**
 * 血猎三阶数值（无铁魔法路径）。
 * attackDamage 是基础近战伤害；silverBonus 是银制武器对吸血鬼的额外伤害；
 * meleeBias 是近战倾向（越高越喜欢近战）。
 */
public enum HunterRank {
    RANK_1(28, 5, 0.35F, 3, 0, 3.0, 240, 300, 2.0F, 0.8F),
    RANK_2(46, 7, 0.35F, 7, 0, 4.0, 200, 280, 4.0F, 0.7F),
    RANK_3(72, 10, 0.36F, 13, 2, 5.0, 180, 260, 6.0F, 0.6F);

    private final double maxHealth;
    private final double attackDamage;
    private final float movementSpeed;
    private final double armor;
    private final double toughness;
    private final double crossbowDamage;
    private final int castIntervalMin;
    private final int castIntervalMax;
    private final float silverBonus;
    private final float meleeBias;

    HunterRank(double maxHealth, double attackDamage, float movementSpeed,
               double armor, double toughness, double crossbowDamage,
               int castIntervalMin, int castIntervalMax, float silverBonus, float meleeBias) {
        this.maxHealth = maxHealth;
        this.attackDamage = attackDamage;
        this.movementSpeed = movementSpeed;
        this.armor = armor;
        this.toughness = toughness;
        this.crossbowDamage = crossbowDamage;
        this.castIntervalMin = castIntervalMin;
        this.castIntervalMax = castIntervalMax;
        this.silverBonus = silverBonus;
        this.meleeBias = meleeBias;
    }

    public double maxHealth() { return maxHealth; }
    public double attackDamage() { return attackDamage; }
    public float movementSpeed() { return movementSpeed; }
    public double armor() { return armor; }
    public double toughness() { return toughness; }
    public double crossbowDamage() { return crossbowDamage; }
    public int castIntervalMin() { return castIntervalMin; }
    public int castIntervalMax() { return castIntervalMax; }
    /** Extra damage dealt to vampire-clan mobs with silver weapons/bolts. */
    public float silverBonus() { return silverBonus; }
    /** 近战倾向 0.6~0.8，越高越主动贴脸近战。 */
    public float meleeBias() { return meleeBias; }
}
