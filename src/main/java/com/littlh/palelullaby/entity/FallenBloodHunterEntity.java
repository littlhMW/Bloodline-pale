package com.littlh.palelullaby.entity;

import com.littlh.palelullaby.PaleLullabyItems;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.joml.Vector3f;

/**
 * 堕落血猎（疯狂阵营）：优先攻击吸血鬼，其次村民与血猎。
 */
public class FallenBloodHunterEntity extends Monster {
    /** 堕落/放逐/初阶/材质变体 四套装备（头/胸/腿/脚），每个部位独立随机实现混搭。 */
    private static final Item[][] FALLEN_ARMOR_TIERS = {
            { PaleLullabyItems.FALLEN_HUNTER_HIGH_HAT.get(), PaleLullabyItems.FALLEN_HUNTER_JACKET.get(),
                    PaleLullabyItems.FALLEN_HUNTER_TROUSERS.get(), PaleLullabyItems.FALLEN_HUNTER_BOOTS.get() },
            { PaleLullabyItems.EXILED_HUNTER_HIGH_HAT.get(), PaleLullabyItems.EXILED_HUNTER_JACKET.get(),
                    PaleLullabyItems.EXILED_HUNTER_TROUSERS.get(), PaleLullabyItems.EXILED_HUNTER_BOOTS.get() },
            { PaleLullabyItems.NOVICE_HUNTER_HIGH_HAT.get(), PaleLullabyItems.NOVICE_HUNTER_JACKET.get(),
                    PaleLullabyItems.NOVICE_HUNTER_TROUSERS.get(), PaleLullabyItems.NOVICE_HUNTER_BOOTS.get() },
            { PaleLullabyItems.CLOTH_HUNTER_HIGH_HAT.get(), PaleLullabyItems.CLOTH_HUNTER_JACKET.get(),
                    PaleLullabyItems.CLOTH_HUNTER_TROUSERS.get(), PaleLullabyItems.CLOTH_HUNTER_ANKLE_BOOTS.get() }
    };
    /** 小概率持有与血猎相同的武器（银剑 + 弩）的概率。 */
    private static final float HUNTER_WEAPON_CHANCE = 0.1F;

    /** 堕落狂热连击计数与目标。 */
    private int combo;
    private int lastTargetId = Integer.MIN_VALUE;
    private int lastComboTick;

    public FallenBloodHunterEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.38D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0D);
    }

    /** 堕落狂热：连续攻击同一目标，每次伤害 +1（最多 +3）。 */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity living)) {
            return false;
        }
        int tid = living.getId();
        if (tid != this.lastTargetId || this.tickCount - this.lastComboTick > 60) {
            this.combo = 0;
            this.lastTargetId = tid;
        }
        this.combo = Math.min(3, this.combo + 1);
        this.lastComboTick = this.tickCount;
        float dmg = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) + this.combo;
        boolean hit = living.hurt(this.damageSources().mobAttack(this), dmg);
        if (hit) {
            DustParticleOptions purple = new DustParticleOptions(new Vector3f(0.4F, 0.1F, 0.5F), 1.0F);
            for (int i = 0; i < 6; i++) {
                this.level().addParticle(purple,
                        living.getX() + this.random.nextGaussian() * 0.5D,
                        living.getY() + this.random.nextDouble() * living.getBbHeight(),
                        living.getZ() + this.random.nextGaussian() * 0.5D, 0, 0, 0);
            }
        }
        return hit;
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide && this.random.nextInt(20) == 0) {
            this.level().addParticle(new DustParticleOptions(new Vector3f(0.4F, 0.1F, 0.5F), 0.8F),
                    this.getX() + this.random.nextGaussian() * 0.4D,
                    this.getY() + this.random.nextDouble() * this.getBbHeight(),
                    this.getZ() + this.random.nextGaussian() * 0.4D, 0, 0, 0);
        }
        super.aiStep();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (!this.level().isClientSide) {
            this.randomizeEquipment();
        }
        return data;
    }

    private void randomizeEquipment() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            int tier = this.random.nextInt(FALLEN_ARMOR_TIERS.length);
            this.setItemSlot(slot, new ItemStack(FALLEN_ARMOR_TIERS[tier][armorSlotIndex(slot)]));
        }
        if (this.random.nextFloat() < HUNTER_WEAPON_CHANCE) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(PaleLullabyItems.SILVER_SWORD.get()));
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.CROSSBOW));
        }
    }

    private static int armorSlotIndex(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            default -> throw new IllegalArgumentException("Not an armor slot: " + slot);
        };
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        FactionTargets.register(this, PaleLullabyFactions.MAD_HUNTER);
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.ZOMBIE_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ZOMBIE_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ZOMBIE_DEATH; }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        // 小概率掉落随机的身上装备
        if (this.random.nextFloat() < 0.2F) {
            EquipmentSlot[] slots = EquipmentSlot.values();
            EquipmentSlot slot = slots[this.random.nextInt(slots.length)];
            ItemStack stack = this.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                if (stack.isDamageableItem()) {
                    int max = Math.max(1, stack.getMaxDamage() / 3);
                    stack.setDamageValue(max + this.random.nextInt(max));
                }
                this.spawnAtLocation(stack);
                this.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
        // 小概率掉落未提纯的血液
        if (this.random.nextFloat() < 0.15F) {
            this.spawnAtLocation(new ItemStack(PaleLullabyItems.UNREFINED_BLOOD_BOTTLE.get()));
        }
    }
}
