package com.littlh.palelullaby.entity;

import com.littlh.palelullaby.entity.minion.PaleMinionEntity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 阵营定义。各实体的仇恨目标统一由 {@link FactionTargets} 注册，
 * 此处只负责“实体属于哪个阵营”和“阵营间的基本关系”。
 *
 * 阵营一览：
 * - VAMPIRE 血族：血族、鲜血贵族、鲜血领主、苍白仆从
 * - HUNTER 血猎：血猎、中阶血猎、高阶血猎
 * - MAD_VAMPIRE 疯狂吸血鬼：枯血鬼（疯狂阵营，先打对方后打自己人）
 * - MAD_HUNTER 疯狂血猎：堕落血猎（疯狂阵营，先打对方后打自己人）
 * - MULLAND 墓兰德：独立 boss，无差别敌对（保留）
 */
public enum PaleLullabyFactions {
    VAMPIRE,
    HUNTER,
    MAD_VAMPIRE,
    MAD_HUNTER,
    MULLAND;

    /** 实体所属阵营；玩家、村民、原版生物等不属于本体系时返回 null。 */
    public static PaleLullabyFactions of(LivingEntity entity) {
        if (entity instanceof MullandEntity) {
            return MULLAND;
        }
        if (entity instanceof DriedBloodGhostEntity) {
            return MAD_VAMPIRE;
        }
        if (entity instanceof FallenBloodHunterEntity) {
            return MAD_HUNTER;
        }
        if (entity instanceof PaleMinionEntity) {
            return VAMPIRE;
        }
        if (entity instanceof VampireMob) {
            return VAMPIRE;
        }
        if (entity instanceof HunterMob) {
            return HUNTER;
        }
        return null;
    }

    /** 血族类（含疯狂吸血鬼）。 */
    public boolean isVampireLike() {
        return this == VAMPIRE || this == MAD_VAMPIRE;
    }

    /** 血猎类（含疯狂血猎）。 */
    public boolean isHunterLike() {
        return this == HUNTER || this == MAD_HUNTER;
    }

    /** 疯狂阵营（疯狂吸血鬼 / 疯狂血猎）。 */
    public boolean isMad() {
        return this == MAD_VAMPIRE || this == MAD_HUNTER;
    }
}
