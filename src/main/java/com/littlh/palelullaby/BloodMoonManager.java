package com.littlh.palelullaby;

import com.littlh.palelullaby.entity.DriedBloodGhostEntity;
import com.littlh.palelullaby.entity.FallenBloodHunterEntity;
import com.littlh.palelullaby.entity.PaleLullabyEntities;
import com.littlh.palelullaby.network.BloodMoonPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 血月状态与全局刷怪：
 * - 全局单例开关，启动时同步给所有客户端；
 * - 主世界进入白天时自动结束；
 * - 血月期间在玩家周围大量刷怪（吸血鬼、枯血鬼、僵尸、骷髅等），并给所有生成的怪物附加增强效果。
 */
@EventBusSubscriber(modid = PaleLullaby.MOD_ID)
public class BloodMoonManager {
    private static boolean active = false;
    private static int spawnTimer = 0;
    private static final ResourceLocation BLOOD_MOON_DAMAGE =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "blood_moon_damage");
    private static final ResourceLocation BLOOD_MOON_SPEED =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "blood_moon_speed");

    public static boolean isActive() {
        return active;
    }

    public static void start(MinecraftServer server) {
        if (active) {
            return;
        }
        active = true;
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld != null) {
            long dayTime = overworld.getDayTime() % 24000L;
            if (dayTime < 13000L) {
                // 若当前处于白天，直接拨到夜晚，保证血月全程是夜晚
                overworld.setDayTime(overworld.getDayTime() - dayTime + 13000L);
            }
        }
        PacketDistributor.sendToAllPlayers(new BloodMoonPayload(true));
    }

    public static void stop(MinecraftServer server) {
        if (!active) {
            return;
        }
        active = false;
        PacketDistributor.sendToAllPlayers(new BloodMoonPayload(false));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (!active) {
            return;
        }
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        // 天亮即自动结束
        if (overworld.isDay()) {
            stop(server);
            return;
        }
        // 周期性在玩家附近大量刷怪
        spawnTimer++;
        if (spawnTimer >= 25) {
            spawnTimer = 0;
            spawnAroundPlayers(server, overworld);
        }
        // 每 5 秒强化玩家附近已有的怪物
        if (overworld.getGameTime() % 100L == 0L) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.level() == overworld) {
                    player.serverLevel().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(64.0))
                            .forEach(BloodMoonManager::applyBloodMoonBuffs);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new BloodMoonPayload(active));
        }
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (active && event.getEntity() instanceof Monster monster) {
            applyBloodMoonBuffs(monster);
        }
    }

    private static void spawnAroundPlayers(MinecraftServer server, ServerLevel overworld) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level() != overworld) {
                continue;
            }
            int count = 1 + overworld.random.nextInt(3);
            for (int i = 0; i < count; i++) {
                spawnOne(overworld, player);
            }
        }
    }

    private static void spawnOne(ServerLevel level, ServerPlayer player) {
        BlockPos center = player.blockPosition();
        double angle = level.random.nextDouble() * Math.PI * 2.0;
        int distance = 20 + level.random.nextInt(25);
        int x = center.getX() + (int) (Math.cos(angle) * distance);
        int z = center.getZ() + (int) (Math.sin(angle) * distance);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, level.getMaxBuildHeight() - 1, z);
        while (pos.getY() > level.getMinBuildHeight() && level.getBlockState(pos).isAir()) {
            pos.move(Direction.DOWN);
        }
        if (pos.getY() <= level.getMinBuildHeight()) {
            return;
        }
        BlockPos spawnPos = pos.above();
        if (!level.getBlockState(spawnPos).isAir() || !level.getBlockState(spawnPos.above()).isAir()) {
            return;
        }
        if (spawnPos.distToCenterSqr(player.getX(), player.getY(), player.getZ()) < 256.0) {
            return;
        }
        Mob mob = pickMob(level);
        if (mob == null) {
            return;
        }
        mob.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        level.addFreshEntity(mob);
        applyBloodMoonBuffs(mob);
    }

    private static Mob pickMob(ServerLevel level) {
        int roll = level.random.nextInt(100);
        if (roll < 30) {
            return PaleLullabyCompat.createVampire(level);
        } else if (roll < 50) {
            return new DriedBloodGhostEntity(PaleLullabyEntities.DRIED_BLOOD_GHOST.get(), level);
        } else if (roll < 65) {
            return new Zombie(level);
        } else if (roll < 77) {
            return EntityType.SKELETON.create(level);
        } else if (roll < 87) {
            return EntityType.SPIDER.create(level);
        } else if (roll < 95) {
            return new FallenBloodHunterEntity(PaleLullabyEntities.FALLEN_BLOOD_HUNTER.get(), level);
        } else {
            return new FallenBloodHunterEntity(PaleLullabyEntities.FALLEN_BLOOD_HUNTER.get(), level);
        }
    }

    /** 血月期间怪物的全局增强：力量 II、迅捷 I、抗性 I。 */
    private static void applyBloodMoonBuffs(Mob mob) {
        AttributeInstance damage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null && !damage.hasModifier(BLOOD_MOON_DAMAGE)) {
            damage.addTransientModifier(new AttributeModifier(BLOOD_MOON_DAMAGE, 1.0, AttributeModifier.Operation.ADD_VALUE));
        }
        AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && !speed.hasModifier(BLOOD_MOON_SPEED)) {
            speed.addTransientModifier(new AttributeModifier(BLOOD_MOON_SPEED, 0.03, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
