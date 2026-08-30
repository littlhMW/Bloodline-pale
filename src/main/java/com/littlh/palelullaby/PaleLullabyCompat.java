package com.littlh.palelullaby;

import com.littlh.palelullaby.entity.BloodHunterEntity;
import com.littlh.palelullaby.entity.HunterRank;
import com.littlh.palelullaby.entity.LordVampireEntity;
import com.littlh.palelullaby.entity.NobleVampireEntity;
import com.littlh.palelullaby.entity.PaleLullabyEntities;
import com.littlh.palelullaby.entity.VampireEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

/**
 * Iron's Spells integration gate. 本类自身不引用任何铁魔法类，
 * 铁魔法专属逻辑集中在 {@link IronSpellsOnlyCompat}，只有在
 * isIronSpellsLoaded() 为 true 时才会被调用（无铁魔法时不会被加载）。
 */
public final class PaleLullabyCompat {
    private PaleLullabyCompat() {
    }

    public static boolean isIronSpellsLoaded() {
        return ModList.get() != null && ModList.get().isLoaded("irons_spellbooks");
    }

    /** Creates the vampire matching the currently registered entity type. */
    public static Mob createVampire(Level level) {
        if (isIronSpellsLoaded()) {
            return IronSpellsOnlyCompat.createIronVampire(level);
        }
        return new VampireEntity(
                (EntityType<? extends Monster>) PaleLullabyEntities.VAMPIRE.get(), level);
    }

    /** Creates the blood noble matching the currently registered entity type. */
    public static Mob createNoble(Level level) {
        if (isIronSpellsLoaded()) {
            return IronSpellsOnlyCompat.createIronNoble(level);
        }
        return new NobleVampireEntity(
                (EntityType<? extends Monster>) PaleLullabyEntities.BLOOD_NOBLE.get(), level);
    }

    /** Creates the blood lord matching the currently registered entity type. */
    public static Mob createLord(Level level) {
        if (isIronSpellsLoaded()) {
            return IronSpellsOnlyCompat.createIronLord(level);
        }
        return new LordVampireEntity(
                (EntityType<? extends Monster>) PaleLullabyEntities.BLOOD_LORD.get(), level);
    }

    /** Creates the novice blood hunter matching the registered entity type. */
    public static Mob createHunter(Level level) {
        if (isIronSpellsLoaded()) {
            return IronSpellsOnlyCompat.createIronHunter(level);
        }
        return new BloodHunterEntity(
                (EntityType<? extends PathfinderMob>) PaleLullabyEntities.BLOOD_HUNTER.get(), level);
    }

    /** Creates the adept blood hunter matching the registered entity type. */
    public static Mob createAdeptHunter(Level level) {
        if (isIronSpellsLoaded()) {
            return IronSpellsOnlyCompat.createIronAdeptHunter(level);
        }
        return new BloodHunterEntity(
                (EntityType<? extends PathfinderMob>) PaleLullabyEntities.ADEPT_BLOOD_HUNTER.get(), level);
    }

    /** Creates the veteran blood hunter matching the registered entity type. */
    public static Mob createVeteranHunter(Level level) {
        if (isIronSpellsLoaded()) {
            return IronSpellsOnlyCompat.createIronVeteranHunter(level);
        }
        return new BloodHunterEntity(
                (EntityType<? extends PathfinderMob>) PaleLullabyEntities.VETERAN_BLOOD_HUNTER.get(), level);
    }

    /** Creates a blood hunter of the given rank (RANK_1/2/3). */
    public static Mob createHunter(HunterRank rank, Level level) {
        return switch (rank) {
            case RANK_2 -> createAdeptHunter(level);
            case RANK_3 -> createVeteranHunter(level);
            default -> createHunter(level);
        };
    }

    /** Creates the mob for the given rank entity type. */
    public static Mob createRank(EntityType<? extends Mob> type, Level level) {
        if (type == PaleLullabyEntities.BLOOD_LORD.get()) {
            return createLord(level);
        }
        if (type == PaleLullabyEntities.BLOOD_NOBLE.get()) {
            return createNoble(level);
        }
        return createVampire(level);
    }
}
