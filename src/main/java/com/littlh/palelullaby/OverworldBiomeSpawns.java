package com.littlh.palelullaby;

import com.littlh.palelullaby.entity.BloodHunterEntity;
import com.littlh.palelullaby.entity.PaleLullabyEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 主世界夜晚小概率刷吸血鬼，白天小概率刷血猎（模组自定义群系除外）。
 */
@EventBusSubscriber(modid = PaleLullaby.MOD_ID)
public class OverworldBiomeSpawns {
    private static final TagKey<Biome> IS_OVERWORLD =
            TagKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("is_overworld"));
    private static int ticker = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (BloodMoonManager.isActive()) {
            return;
        }
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        if (++ticker % 600 != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level() != overworld) {
                continue;
            }
            if (overworld.random.nextFloat() >= 0.30F) {
                continue;
            }
            trySpawn(overworld, player);
        }
    }

    private static void trySpawn(ServerLevel level, ServerPlayer player) {
        boolean day = level.isDay();
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            int distance = 24 + level.random.nextInt(24);
            int x = player.getBlockX() + (int) (Math.cos(angle) * distance);
            int z = player.getBlockZ() + (int) (Math.sin(angle) * distance);
            BlockPos spawnPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, player.getBlockY(), z)).above();
            if (!level.getBlockState(spawnPos).isAir() || !level.getBlockState(spawnPos.above()).isAir()) {
                continue;
            }
            if (spawnPos.distToCenterSqr(player.getX(), player.getY(), player.getZ()) < 256.0) {
                continue;
            }
            Holder<Biome> biome = level.getBiome(spawnPos);
            if (!biome.is(IS_OVERWORLD)) {
                continue;
            }
            ResourceKey<Biome> key = biome.unwrapKey().orElse(null);
            if (key != null && key.location().getNamespace().equals(PaleLullaby.MOD_ID)) {
                continue;
            }
            if (day) {
                if (!Mob.checkMobSpawnRules(PaleLullabyEntities.BLOOD_HUNTER.get(), level, MobSpawnType.NATURAL, spawnPos, level.random)) {
                    continue;
                }
                Mob hunter = PaleLullabyCompat.createHunter(level);
                hunter.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                level.addFreshEntity(hunter);
                return;
            } else {
                float roll = level.random.nextFloat();
                EntityType<? extends Mob> chosen = PaleLullabyEntities.VAMPIRE.get();
                if (PaleLullabyCompat.isIronSpellsLoaded()) {
                    chosen = roll < 0.03F ? PaleLullabyEntities.BLOOD_LORD.get()
                            : roll < 0.15F ? PaleLullabyEntities.BLOOD_NOBLE.get()
                            : PaleLullabyEntities.VAMPIRE.get();
                }
                if (!Mob.checkMobSpawnRules(chosen, level, MobSpawnType.NATURAL, spawnPos, level.random)) {
                    continue;
                }
                Mob mob = PaleLullabyCompat.createRank(chosen, level);
                mob.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                level.addFreshEntity(mob);
                return;
            }
        }
    }
}