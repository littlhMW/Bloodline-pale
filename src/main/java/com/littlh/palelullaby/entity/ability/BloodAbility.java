package com.littlh.palelullaby.entity.ability;

import net.minecraft.resources.ResourceLocation;

/**
 * 自写能力的数据描述。实际效果在施法者的 {@code performAbility} 里实现；
 * 这个 record 只驱动 AI 目标（射程、读条、冷却、伤害）并携带决策权重。
 * {@code weight} 是基础权重，AbilityCastGoal 会按距离/血量再放大。
 */
public record BloodAbility(
        ResourceLocation id,
        AbilityType type,
        int castTime,
        int cooldownMin,
        int cooldownMax,
        double range,
        float damage,
        int weight) {

    public static BloodAbility ability(String id, AbilityType type, int castTime,
                                       int cooldownMin, int cooldownMax, double range,
                                       float damage, int weight) {
        return new BloodAbility(ResourceLocation.fromNamespaceAndPath("pale_lullaby", id), type,
                castTime, cooldownMin, cooldownMax, range, damage, weight);
    }
}
