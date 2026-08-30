package com.littlh.palelullaby;

import com.littlh.palelullaby.entity.BloodLordEntity;
import com.littlh.palelullaby.entity.BloodNobleEntity;
import com.littlh.palelullaby.entity.HunterRank;
import com.littlh.palelullaby.entity.PaleLullabyEntities;
import com.littlh.palelullaby.entity.SpellCastingBloodHunterEntity;
import com.littlh.palelullaby.entity.SpellCastingVampireEntity;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * 铁魔法（irons_spellbooks）专属兼容代码。本类只在铁魔法已加载时才被调用，
 * 无铁魔法时不会被任何主路径引用，因此可以安全地直接引用铁魔法类。
 * 所有入口方法签名避免使用铁魔法类型，外部仅通过 invokestatic 调用。
 */
public final class IronSpellsOnlyCompat {
    private IronSpellsOnlyCompat() {
    }

    public static Mob createIronVampire(Level level) {
        return new SpellCastingVampireEntity(
                (EntityType<? extends AbstractSpellCastingMob>) PaleLullabyEntities.VAMPIRE.get(), level);
    }

    public static Mob createIronNoble(Level level) {
        return new BloodNobleEntity(
                (EntityType<? extends AbstractSpellCastingMob>) PaleLullabyEntities.BLOOD_NOBLE.get(), level);
    }

    public static Mob createIronLord(Level level) {
        return new BloodLordEntity(
                (EntityType<? extends AbstractSpellCastingMob>) PaleLullabyEntities.BLOOD_LORD.get(), level);
    }

    public static Mob createIronHunter(Level level) {
        return new SpellCastingBloodHunterEntity(
                (EntityType<? extends AbstractSpellCastingMob>) PaleLullabyEntities.BLOOD_HUNTER.get(), level);
    }

    public static EntityType<? extends Mob> buildIronVampireType() {
        return EntityType.Builder.<SpellCastingVampireEntity>of(SpellCastingVampireEntity::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F)
                .clientTrackingRange(8)
                .build("vampire");
    }

    public static EntityType<? extends Mob> buildIronNobleType() {
        return EntityType.Builder.<BloodNobleEntity>of(BloodNobleEntity::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F)
                .clientTrackingRange(8)
                .build("blood_noble");
    }

    public static EntityType<? extends Mob> buildIronLordType() {
        return EntityType.Builder.<BloodLordEntity>of(BloodLordEntity::new, MobCategory.MONSTER)
                .sized(0.6F, 1.95F)
                .clientTrackingRange(8)
                .build("blood_lord");
    }

    public static Mob createIronAdeptHunter(Level level) {
        return new SpellCastingBloodHunterEntity(
                (EntityType<? extends AbstractSpellCastingMob>) PaleLullabyEntities.ADEPT_BLOOD_HUNTER.get(), level);
    }

    public static Mob createIronVeteranHunter(Level level) {
        return new SpellCastingBloodHunterEntity(
                (EntityType<? extends AbstractSpellCastingMob>) PaleLullabyEntities.VETERAN_BLOOD_HUNTER.get(), level);
    }

    public static EntityType<? extends Mob> buildIronHunterType() {
        return EntityType.Builder.<SpellCastingBloodHunterEntity>of(SpellCastingBloodHunterEntity::new, MobCategory.CREATURE)
                .sized(0.6F, 1.95F)
                .clientTrackingRange(8)
                .build("blood_hunter");
    }

    public static EntityType<? extends Mob> buildIronAdeptHunterType() {
        return EntityType.Builder.<SpellCastingBloodHunterEntity>of(SpellCastingBloodHunterEntity::new, MobCategory.CREATURE)
                .sized(0.6F, 1.95F)
                .clientTrackingRange(8)
                .build("adept_blood_hunter");
    }

    public static EntityType<? extends Mob> buildIronVeteranHunterType() {
        return EntityType.Builder.<SpellCastingBloodHunterEntity>of(SpellCastingBloodHunterEntity::new, MobCategory.CREATURE)
                .sized(0.6F, 1.95F)
                .clientTrackingRange(8)
                .build("veteran_blood_hunter");
    }

    public static void registerIronSpellAttributes(EntityAttributeCreationEvent event) {
        event.put(PaleLullabyEntities.VAMPIRE.get(), SpellCastingVampireEntity.createAttributes().build());
        event.put(PaleLullabyEntities.BLOOD_NOBLE.get(), BloodNobleEntity.createAttributes().build());
        event.put(PaleLullabyEntities.BLOOD_LORD.get(), BloodLordEntity.createAttributes().build());
        event.put(PaleLullabyEntities.BLOOD_HUNTER.get(), SpellCastingBloodHunterEntity.createAttributes(HunterRank.RANK_1).build());
        event.put(PaleLullabyEntities.ADEPT_BLOOD_HUNTER.get(), SpellCastingBloodHunterEntity.createAttributes(HunterRank.RANK_2).build());
        event.put(PaleLullabyEntities.VETERAN_BLOOD_HUNTER.get(), SpellCastingBloodHunterEntity.createAttributes(HunterRank.RANK_3).build());
    }
}
