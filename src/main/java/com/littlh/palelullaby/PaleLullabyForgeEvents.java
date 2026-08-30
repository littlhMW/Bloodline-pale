package com.littlh.palelullaby;

import com.littlh.palelullaby.entity.BloodHunterEntity;
import com.littlh.palelullaby.entity.HunterMob;
import com.littlh.palelullaby.entity.HunterRank;
import com.littlh.palelullaby.entity.DriedBloodGhostEntity;
import com.littlh.palelullaby.entity.FallenBloodHunterEntity;
import com.littlh.palelullaby.entity.MullandEntity;
import com.littlh.palelullaby.entity.PaleLullabyEntities;
import com.littlh.palelullaby.entity.VampireMob;
import com.littlh.palelullaby.entity.PaleLullabyFactions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BlockToolModificationEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 战斗与状态事件：
 * 1) 银剑对亡灵（含吸血鬼）伤害 ×2.5，对血猎 ×1.5；
 * 2) 猩红花园/荒芜高原中亡灵不会在阳光下燃烧。
 */
@EventBusSubscriber(modid = PaleLullaby.MOD_ID)
public class PaleLullabyForgeEvents {
    private static final TagKey<EntityType<?>> UNDEAD = TagKey.create(
            Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "undead"));
    private static final TagKey<EntityType<?>> VAMPIRE = TagKey.create(
            Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "vampire"));
    private static final TagKey<net.minecraft.world.item.Item> SILVER_TOOLS = TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "silver_tools"));
    private static final TagKey<net.minecraft.world.item.Item> SILVER_ARMOR = TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "silver_armor"));

    /** 记录实体最近一次实际受击伤害（供 FactionTargets 判定“主动攻击”用）。 */
    private static final Map<UUID, Float> LAST_HIT_DAMAGE = new ConcurrentHashMap<>();

    /** 取出并清除指定实体的最近受击伤害；无记录返回 0。 */
    public static float takeLastHitDamage(LivingEntity entity) {
        Float dmg = LAST_HIT_DAMAGE.remove(entity.getUUID());
        return dmg == null ? 0.0F : dmg;
    }

    @SubscribeEvent
    public static void onToolModification(BlockToolModificationEvent event) {
        // 用锄头在苍白余烬上开垦出苍白余烬耕地
        if (event.getItemAbility() == ItemAbilities.HOE_TILL
                && event.getState().is(PaleLullabyBlocks.PALE_EMBER.get())) {
            event.setFinalState(PaleLullabyBlocks.PALE_EMBER_FARMLAND.get().defaultBlockState());
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof Villager villager) {
            boolean alreadyFleeing = villager.goalSelector.getAvailableGoals().stream()
                    .anyMatch(g -> g.getGoal() instanceof AvoidEntityGoal);
            if (!alreadyFleeing) {
                villager.goalSelector.addGoal(1, new AvoidEntityGoal<>(villager, LivingEntity.class,
                        10.0F, 0.9D, 1.3D,
                        e -> {
                            PaleLullabyFactions f = PaleLullabyFactions.of(e);
                            return f != null && !f.isHunterLike();
                        }));
            }
        }
    }

    /** 血猎营地：结构里的 marker 落地时替换成 1~4 只随机阶级血猎（外观装备按血猎库随机）。 */
    @SubscribeEvent
    public static void onCampHunterMarker(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.MARKER || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        if (entity.getCustomName() == null || !"pl_camp_hunter".equals(entity.getCustomName().getString())) {
            return;
        }
        event.setCanceled(true);
        BlockPos anchor = entity.blockPosition();
        level.getServer().execute(() -> spawnCampHunters(level, anchor));
    }

    /** 在营地内生成 1~4 只随机阶级血猎，落点取营地地面高度。 */
    private static void spawnCampHunters(ServerLevel level, BlockPos anchor) {
        int count = 1 + level.random.nextInt(4);
        for (int i = 0; i < count; i++) {
            BlockPos pos = campSpawnPos(level, anchor);
            if (pos == null) {
                continue;
            }
            HunterRank rank = campRank(level.random);
            Mob hunter = PaleLullabyCompat.createHunter(rank, level);
            hunter.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            hunter.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
            if (hunter instanceof HunterMob hunterMob) {
                hunterMob.equipCampGear();
            }
            level.addFreshEntity(hunter);
        }
    }

    private static BlockPos campSpawnPos(ServerLevel level, BlockPos anchor) {
        for (int attempt = 0; attempt < 8; attempt++) {
            int dx = level.random.nextInt(15) - 7;
            int dz = level.random.nextInt(11) - 5;
            BlockPos base = anchor.offset(dx, 0, dz);
            BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base);
            BlockPos spawn = ground.above();
            if (level.getBlockState(spawn).isAir() && level.getBlockState(spawn.above()).isAir()) {
                return spawn;
            }
        }
        BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, anchor);
        return ground.above();
    }

    /** 营地阶级分布：初阶最常见，高阶最稀有。 */
    private static HunterRank campRank(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.5F) {
            return HunterRank.RANK_1;
        }
        if (roll < 0.83F) {
            return HunterRank.RANK_2;
        }
        return HunterRank.RANK_3;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }
        DamageSource source = event.getSource();
        Entity direct = source.getDirectEntity();
        if (!(direct instanceof Player player)) {
            return;
        }
        ItemStack weapon = player.getMainHandItem();
        // 银剑的伤害加成应用到所有银工具
        if (!weapon.is(SILVER_TOOLS)) {
            return;
        }
        float multiplier;
        if (target.getType().is(UNDEAD)) {
            multiplier = 2.5F;
        } else if (target instanceof BloodHunterEntity || target instanceof FallenBloodHunterEntity) {
            multiplier = 1.5F;
        } else {
            return;
        }
        event.setNewDamage(event.getNewDamage() * multiplier);
    }

    /** 被吸血鬼近战攻击时，有概率让玩家获得血疾。 */
    @SubscribeEvent
    public static void onVampireMeleeHit(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.isSpectator() || player.isCreative()) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker == null) {
            return;
        }
        boolean vampire = attacker.getType().is(VAMPIRE) || attacker instanceof VampireMob;
        if (!vampire) {
            return;
        }
        if (player.getRandom().nextDouble() < 0.15D) {
            // 永久血疾（-1 = 无限时长），可用牛奶解除
            player.addEffect(new MobEffectInstance(PaleLullabyEffects.BLOOD_FRENZY,
                    -1, 0, false, true, true));
        }
    }

    /** 本 MOD 的永久效果不能被牛奶等治愈方式（cure）解除。 */
    @SubscribeEvent
    public static void onEffectCureAttempt(MobEffectEvent.Remove event) {
        if (event.getCure() == null) {
            return;
        }
        net.minecraft.core.Holder<MobEffect> effect = event.getEffect();
        if (effect.is(PaleLullabyEffects.BLOOD_FRENZY.getKey())
                || effect.is(PaleLullabyEffects.BLOOD_THIRST.getKey())) {
            event.setCanceled(true);
        }
    }

    /** 寡妇刺：附近生物流血时，概率让未开花的寡妇刺开出灰白小花（约3天后母株枯死）。 */
    @SubscribeEvent
    public static void onBloodSpilled(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (!target.level().isClientSide) {
            LAST_HIT_DAMAGE.put(target.getUUID(), event.getNewDamage());
        }
        Level level = target.level();
        // 吸血光环：攻击命中按伤害 25% 回血
        if (event.getSource().getEntity() instanceof LivingEntity attacker
                && attacker.isAlive() && attacker.hasEffect(PaleLullabyEffects.BLOOD_AURA)) {
            attacker.heal(event.getNewDamage() * 0.25F);
        }
        if (level.isClientSide || event.getNewDamage() <= 0.0F) {
            return;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }
        BlockPos center = target.blockPosition();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
                    if (!state.is(PaleLullabyBlocks.WIDOW_THORN.get())
                            || state.getValue(WidowThornBlock.HALF) != net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER
                            || state.getValue(WidowThornBlock.STAGE) != WidowThornBlock.Stage.DORMANT) {
                        continue;
                    }
                    if (level.random.nextFloat() < 0.5F) {
                        net.minecraft.world.level.block.state.BlockState newLower = state.setValue(WidowThornBlock.STAGE, WidowThornBlock.Stage.FLOWERING)
                                .setValue(WidowThornBlock.AGE, 3);
                        level.setBlock(pos, newLower, 2);
                        BlockPos upperPos = pos.above();
                        net.minecraft.world.level.block.state.BlockState upper = level.getBlockState(upperPos);
                        if (upper.is(PaleLullabyBlocks.WIDOW_THORN.get())) {
                            level.setBlock(upperPos, upper.setValue(WidowThornBlock.STAGE, WidowThornBlock.Stage.FLOWERING)
                                    .setValue(WidowThornBlock.AGE, 3), 2);
                        }
                    }
                }
            }
        }
    }

    /** 荆棘之肤：受到近战攻击时反弹 3 点荆棘伤害。 */
    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide || !victim.hasEffect(PaleLullabyEffects.THORN_SKIN)) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.is(net.minecraft.world.damagesource.DamageTypes.THORNS)
                || !(source.getEntity() instanceof LivingEntity attacker) || attacker.isDeadOrDying()) {
            return;
        }
        if (victim.distanceToSqr(attacker) > 16.0D) {
            return;
        }
        attacker.hurt(victim.damageSources().thorns(victim), 3.0F);
    }

    /** 腐血：受到的直接治疗效果减半。 */
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(PaleLullabyEffects.BLOOD_ROT)
                || entity.hasEffect(PaleLullabyEffects.WITHERING_TOUCH)) {
            event.setAmount(event.getAmount() * 0.5F);
        }
    }

    /** 腐心菇：非玩家生物死在腐心菇上时，掉落的残骸被分解为骨头。 */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (level.isClientSide || entity instanceof Player) {
            return;
        }
        if (!entity.getBlockStateOn().is(PaleLullabyBlocks.ROTHEART_MUSHROOM.get())) {
            return;
        }
        event.getDrops().clear();
        int count = 1 + entity.getRandom().nextInt(2);
        for (int i = 0; i < count; i++) {
            event.getDrops().add(new ItemEntity(level,
                    entity.getX(), entity.getY(), entity.getZ(), new ItemStack(Items.BONE)));
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }
        // 安魂/假死：清空对持有对应效果玩家的仇恨（类似创造模式 AI 忽略）
        if (entity instanceof Mob mob && mob.getTarget() instanceof Player p
                && (p.hasEffect(PaleLullabyEffects.REQUIEM) || p.hasEffect(PaleLullabyEffects.FEIGNED_DEATH))) {
            mob.setTarget(null);
        }
        // 吸血鬼穿戴银盔甲会持续受伤
        if (living.getType().is(VAMPIRE)) {
            boolean wearingSilver = false;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR
                        && living.getItemBySlot(slot).is(SILVER_ARMOR)) {
                    wearingSilver = true;
                    break;
                }
            }
            if (wearingSilver && living.tickCount % 20 == 0) {
                living.hurt(living.damageSources().magic(), 2.0F);
            }
        }
        if (living.getRemainingFireTicks() <= 0) {
            return;
        }
        if (!living.getType().is(UNDEAD)) {
            return;
        }
        Level level = living.level();
        if (!isShelteredBiome(level, living.blockPosition())) {
            return;
        }
        if (living.isInLava()) {
            return;
        }
        // 只清除太阳点燃：白天、可见天空、头部无装备
        if (!level.isDay()) {
            return;
        }
        if (!level.canSeeSky(BlockPos.containing(living.getX(), living.getEyeY(), living.getZ()))) {
            return;
        }
        if (!living.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            return;
        }
        living.clearFire();
    }


    /** 安魂/假死：敌人不会把持有对应效果的玩家设为目标（类似创造模式 AI 忽略）。 */
    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof Player player
                && (player.hasEffect(PaleLullabyEffects.REQUIEM) || player.hasEffect(PaleLullabyEffects.FEIGNED_DEATH))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();
        // 雾系：盲絮花/幽灵兰花 + 幻翼膜
        builder.addMix(Potions.WATER, PaleLullabyItems.BLIND_FLOSS_FLOWER_ITEM.get(), PaleLullabyPotions.MIST_ESSENCE);
        builder.addMix(PaleLullabyPotions.MIST_ESSENCE, Items.PHANTOM_MEMBRANE, PaleLullabyPotions.MIST_REQUIEM);
        builder.addMix(Potions.WATER, PaleLullabyItems.GHOST_ORCHID_ITEM.get(), PaleLullabyPotions.ORCHID_ESSENCE);
        builder.addMix(PaleLullabyPotions.ORCHID_ESSENCE, Items.PHANTOM_MEMBRANE, PaleLullabyPotions.GHOST_ORCHID_DEW);
        // 血系：猩红荆棘/腐心菇/猩红刺果
        builder.addMix(Potions.WATER, PaleLullabyItems.CRIMSON_THORN_ITEM.get(), PaleLullabyPotions.THORN_ESSENCE);
        builder.addMix(PaleLullabyPotions.THORN_ESSENCE, Items.FERMENTED_SPIDER_EYE, PaleLullabyPotions.BLOOD_THORN_TOUCH);
        builder.addMix(Potions.WATER, PaleLullabyItems.ROTHEART_MUSHROOM_ITEM.get(), PaleLullabyPotions.ROT_ESSENCE);
        builder.addMix(PaleLullabyPotions.ROT_ESSENCE, Items.FERMENTED_SPIDER_EYE, PaleLullabyPotions.ROTHEART_DEW);
        builder.addMix(Potions.WATER, PaleLullabyItems.CRIMSON_THORN_BERRY.get(), PaleLullabyPotions.BERRY_ESSENCE);
        builder.addMix(PaleLullabyPotions.BERRY_ESSENCE, Items.SUGAR, PaleLullabyPotions.THORN_BERRY_BREW);
        // 蔷薇：猩红蔷薇 + 蜂蜜瓶
        builder.addMix(Potions.WATER, PaleLullabyItems.CRIMSON_ROSE_ITEM.get(), PaleLullabyPotions.ROSE_ESSENCE);
        builder.addMix(PaleLullabyPotions.ROSE_ESSENCE, Items.HONEY_BOTTLE, PaleLullabyPotions.ROSE_MEAD);
        // 凋萎槲寄生系：麻痹 / 假死
        builder.addMix(Potions.WATER, PaleLullabyItems.WITHERED_MISTLETOE_ITEM.get(), PaleLullabyPotions.WITHERED_ESSENCE);
        builder.addMix(PaleLullabyPotions.WITHERED_ESSENCE, Items.FERMENTED_SPIDER_EYE, PaleLullabyPotions.WEAKNESS_OIL);
        builder.addMix(PaleLullabyPotions.WITHERED_ESSENCE, Items.WHEAT, PaleLullabyPotions.FEIGNED_DEATH);
    }

    static boolean isShelteredBiome(Level level, BlockPos pos) {
        return level.getBiome(pos).is(PaleLullabyBiomes.CRIMSON_GARDEN)
                || level.getBiome(pos).is(PaleLullabyBiomes.WITHERED_PLATEAU);
    }
}
