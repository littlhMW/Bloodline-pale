package com.littlh.palelullaby.entity;

import com.littlh.palelullaby.entity.ability.BloodAbility;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * A mob that can channel self-written blood/silver abilities. The casting
 * state is what the client renderer reads to pose the casting animation.
 * Works without Iron's Spells; Iron-based casters implement it as a thin
 * wrapper around their own casting state.
 */
public interface CasterMob {
    boolean isCasting();
    void setCasting(boolean casting);
    List<BloodAbility> abilities();
    void performAbility(BloodAbility ability, LivingEntity target);

    /** 施法前的额外条件判断（例如净化药剂只在有负面效果时使用）。 */
    default boolean canCastAbility(BloodAbility ability, LivingEntity target) {
        return true;
    }
}
