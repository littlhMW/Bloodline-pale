package com.littlh.palelullaby.entity;

import com.littlh.palelullaby.PaleLullaby;
import com.littlh.palelullaby.PaleLullabyCompat;
import com.littlh.palelullaby.IronSpellsOnlyCompat;
import com.littlh.palelullaby.entity.minion.PaleMinionEntity;
import com.littlh.palelullaby.entity.projectile.BloodNeedleProjectile;
import com.littlh.palelullaby.entity.projectile.SilverBoltProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class PaleLullabyEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, PaleLullaby.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<MullandEntity>> MULLAND =
            ENTITY_TYPES.register("mulland",
                    () -> EntityType.Builder.of(MullandEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .build("mulland"));

    public static final DeferredHolder<EntityType<?>, EntityType<PaleMinionEntity>> PALE_MINION =
            ENTITY_TYPES.register("pale_minion",
                    () -> EntityType.Builder.of(PaleMinionEntity::new, MobCategory.MONSTER)
                            .sized(0.7F, 0.8F)
                            .clientTrackingRange(8)
                            .build("pale_minion"));

    /**
     * Vampire entity. With Iron's Spells installed the registered class is
     * the blood-magic caster; otherwise it falls back to the self-written
     * caster.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<? extends Mob>> VAMPIRE =
            ENTITY_TYPES.register("vampire", PaleLullabyEntities::buildVampireType);

    /**
     * Blood hunter (novice rank). With Iron's Spells installed it channels
     * the Holy school; otherwise it uses the self-written silver abilities.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<? extends Mob>> BLOOD_HUNTER =
            ENTITY_TYPES.register("blood_hunter", PaleLullabyEntities::buildHunterType);

    /** Blood hunter (adept rank). */
    public static final DeferredHolder<EntityType<?>, EntityType<? extends Mob>> ADEPT_BLOOD_HUNTER =
            ENTITY_TYPES.register("adept_blood_hunter", PaleLullabyEntities::buildAdeptHunterType);

    /** Blood hunter (veteran rank). */
    public static final DeferredHolder<EntityType<?>, EntityType<? extends Mob>> VETERAN_BLOOD_HUNTER =
            ENTITY_TYPES.register("veteran_blood_hunter", PaleLullabyEntities::buildVeteranHunterType);

    public static final DeferredHolder<EntityType<?>, EntityType<FallenBloodHunterEntity>> FALLEN_BLOOD_HUNTER =
            ENTITY_TYPES.register("fallen_blood_hunter",
                    () -> EntityType.Builder.of(FallenBloodHunterEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .build("fallen_blood_hunter"));

    /** Blood noble. Casting class with Iron, self-written caster without. */
    public static final DeferredHolder<EntityType<?>, EntityType<? extends Mob>> BLOOD_NOBLE =
            ENTITY_TYPES.register("blood_noble", PaleLullabyEntities::buildNobleType);

    /** Blood lord. Casting class with Iron, self-written caster without. */
    public static final DeferredHolder<EntityType<?>, EntityType<? extends Mob>> BLOOD_LORD =
            ENTITY_TYPES.register("blood_lord", PaleLullabyEntities::buildLordType);

    public static final DeferredHolder<EntityType<?>, EntityType<DriedBloodGhostEntity>> DRIED_BLOOD_GHOST =
            ENTITY_TYPES.register("dried_blood_ghost",
                    () -> EntityType.Builder.of(DriedBloodGhostEntity::new, MobCategory.MONSTER)
                            .sized(0.9F, 0.9F)
                            .clientTrackingRange(8)
                            .build("dried_blood_ghost"));

    public static final DeferredHolder<EntityType<?>, EntityType<TolandBatEntity>> TOLAND_BAT =
            ENTITY_TYPES.register("toland_bat",
                    () -> EntityType.Builder.of(TolandBatEntity::new, MobCategory.CREATURE)
                            .sized(1.4F, 1.8F)
                            .clientTrackingRange(10)
                            .build("toland_bat"));

    public static final DeferredHolder<EntityType<?>, EntityType<BloodNeedleProjectile>> BLOOD_NEEDLE =
            ENTITY_TYPES.register("blood_needle",
                    () -> EntityType.Builder.<BloodNeedleProjectile>of(BloodNeedleProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .build("blood_needle"));

    public static final DeferredHolder<EntityType<?>, EntityType<SilverBoltProjectile>> SILVER_BOLT =
            ENTITY_TYPES.register("silver_bolt",
                    () -> EntityType.Builder.<SilverBoltProjectile>of(SilverBoltProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .build("silver_bolt"));

    private static EntityType<? extends Mob> buildVampireType() {
        if (PaleLullabyCompat.isIronSpellsLoaded()) {
            return IronSpellsOnlyCompat.buildIronVampireType();
        }
        return buildVanillaVampireType();
    }

    private static EntityType<? extends Mob> buildVanillaVampireType() {
        return EntityType.Builder.<VampireEntity>of(VampireEntity::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F)
                .clientTrackingRange(8)
                .build("vampire");
    }

    private static EntityType<? extends Mob> buildNobleType() {
        if (PaleLullabyCompat.isIronSpellsLoaded()) {
            return IronSpellsOnlyCompat.buildIronNobleType();
        }
        return buildVanillaNobleType();
    }

    private static EntityType<? extends Mob> buildVanillaNobleType() {
        return EntityType.Builder.<NobleVampireEntity>of(NobleVampireEntity::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F)
                .clientTrackingRange(8)
                .build("blood_noble");
    }

    private static EntityType<? extends Mob> buildLordType() {
        if (PaleLullabyCompat.isIronSpellsLoaded()) {
            return IronSpellsOnlyCompat.buildIronLordType();
        }
        return buildVanillaLordType();
    }

    private static EntityType<? extends Mob> buildVanillaLordType() {
        return EntityType.Builder.<LordVampireEntity>of(LordVampireEntity::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F)
                .clientTrackingRange(8)
                .build("blood_lord");
    }

    private static EntityType<? extends Mob> buildHunterType() {
        if (PaleLullabyCompat.isIronSpellsLoaded()) {
            return IronSpellsOnlyCompat.buildIronHunterType();
        }
        return buildVanillaHunterType();
    }

    private static EntityType<? extends Mob> buildVanillaHunterType() {
        return EntityType.Builder.<BloodHunterEntity>of(BloodHunterEntity::new, MobCategory.CREATURE)
                .sized(0.6F, 1.95F)
                .clientTrackingRange(8)
                .build("blood_hunter");
    }

    private static EntityType<? extends Mob> buildAdeptHunterType() {
        if (PaleLullabyCompat.isIronSpellsLoaded()) {
            return IronSpellsOnlyCompat.buildIronAdeptHunterType();
        }
        return buildVanillaAdeptHunterType();
    }

    private static EntityType<? extends Mob> buildVanillaAdeptHunterType() {
        return EntityType.Builder.<BloodHunterEntity>of(BloodHunterEntity::new, MobCategory.CREATURE)
                .sized(0.6F, 1.95F)
                .clientTrackingRange(8)
                .build("adept_blood_hunter");
    }

    private static EntityType<? extends Mob> buildVeteranHunterType() {
        if (PaleLullabyCompat.isIronSpellsLoaded()) {
            return IronSpellsOnlyCompat.buildIronVeteranHunterType();
        }
        return buildVanillaVeteranHunterType();
    }

    private static EntityType<? extends Mob> buildVanillaVeteranHunterType() {
        return EntityType.Builder.<BloodHunterEntity>of(BloodHunterEntity::new, MobCategory.CREATURE)
                .sized(0.6F, 1.95F)
                .clientTrackingRange(8)
                .build("veteran_blood_hunter");
    }
}
