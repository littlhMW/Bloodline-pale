package com.littlh.palelullaby;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 自定义状态效果注册。 */
public class PaleLullabyEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, PaleLullaby.MOD_ID);

    /** 血疾：视野严重发红+恶心，狂暴攻击附近村民/掠夺者/玩家。 */
    public static final DeferredHolder<MobEffect, BloodFrenzyEffect> BLOOD_FRENZY =
            EFFECTS.register("blood_frenzy", BloodFrenzyEffect::new);

    /** 渴血：随时间升级/降级，3级后可能恶化为血疾。 */
    public static final DeferredHolder<MobEffect, BloodThirstEffect> BLOOD_THIRST =
            EFFECTS.register("blood_thirst", BloodThirstEffect::new);

    /** 安魂：身周雾气，持有者不会被生物设为目标。 */
    public static final DeferredHolder<MobEffect, RequiemEffect> REQUIEM =
            EFFECTS.register("requiem", RequiemEffect::new);
    /** 幽体漫步：半透明，怪物索敌范围缩小。 */
    public static final DeferredHolder<MobEffect, GhostWalkEffect> GHOST_WALK =
            EFFECTS.register("ghost_walk", GhostWalkEffect::new);
    /** 吸血光环：攻击命中恢复 25% 伤害的生命。 */
    public static final DeferredHolder<MobEffect, BloodAuraEffect> BLOOD_AURA =
            EFFECTS.register("blood_aura", BloodAuraEffect::new);
    /** 蔷薇蜜意：缓慢回血，抑制渴血症状。 */
    public static final DeferredHolder<MobEffect, RoseNectarEffect> ROSE_NECTAR =
            EFFECTS.register("rose_nectar", RoseNectarEffect::new);
    /** 腐血：持续流失生命，治疗效果减半。 */
    public static final DeferredHolder<MobEffect, BloodRotEffect> BLOOD_ROT =
            EFFECTS.register("blood_rot", BloodRotEffect::new);
    /** 荆棘之肤：受近战攻击反弹伤害并消耗装备耐久。 */
    public static final DeferredHolder<MobEffect, ThornSkinEffect> THORN_SKIN =
            EFFECTS.register("thorn_skin", ThornSkinEffect::new);
    /** 麻痹：虚弱之油，肌肉松弛无力（移速/攻击大幅下降）。 */
    public static final DeferredHolder<MobEffect, ParalysisEffect> PARALYSIS =
            EFFECTS.register("paralysis", ParalysisEffect::new);
    /** 假死：深度昏迷，几乎无法移动；配合隐形骗过掠食者。 */
    public static final DeferredHolder<MobEffect, FeignDeathEffect> FEIGNED_DEATH =
            EFFECTS.register("feigned_death", FeignDeathEffect::new);
    /** 血疫：吸血鬼诅咒，持续掉血并减速，可被净化。 */
    public static final DeferredHolder<MobEffect, SanguinePlagueEffect> SANGUINE_PLAGUE =
            EFFECTS.register("sanguine_plague", SanguinePlagueEffect::new);
    /** 枯竭之触：枯血鬼标记，治疗减半。 */
    public static final DeferredHolder<MobEffect, WitheringTouchEffect> WITHERING_TOUCH =
            EFFECTS.register("withering_touch", WitheringTouchEffect::new);
}
