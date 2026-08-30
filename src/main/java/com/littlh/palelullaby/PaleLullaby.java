package com.littlh.palelullaby;

import com.littlh.palelullaby.entity.PaleLullabyEntities;
import com.littlh.palelullaby.entity.MullandEntity;
import com.littlh.palelullaby.entity.BloodHunterEntity;
import com.littlh.palelullaby.entity.HunterRank;
import com.littlh.palelullaby.entity.LordVampireEntity;
import com.littlh.palelullaby.entity.NobleVampireEntity;
import com.littlh.palelullaby.entity.VampireEntity;
import com.littlh.palelullaby.entity.FallenBloodHunterEntity;
import com.littlh.palelullaby.entity.DriedBloodGhostEntity;
import com.littlh.palelullaby.entity.minion.PaleMinionEntity;
import com.littlh.palelullaby.entity.TolandBatEntity;
import com.littlh.palelullaby.fluid.ModFluids;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@Mod("pale_lullaby")
public class PaleLullaby {
    public static final String MOD_ID = "pale_lullaby";

    public PaleLullaby(IEventBus modEventBus) {
        PaleLullabyBlocks.BLOCKS.register(modEventBus);
        PaleLullabyBlocks.BLOCK_ENTITIES.register(modEventBus);
        PaleLullabyItems.ITEMS.register(modEventBus);
        PaleLullabyEffects.EFFECTS.register(modEventBus);
        PaleLullabyPotions.POTIONS.register(modEventBus);
        PaleLullabyArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        PaleLullabyFeatures.FEATURES.register(modEventBus);
        PaleLullabyEntities.ENTITY_TYPES.register(modEventBus);
        ModFluids.register(modEventBus);
        PaleLullabyTabs.CREATIVE_TABS.register(modEventBus);
        ModSounds.register(modEventBus);
        PaleLullabyParticles.PARTICLES.register(modEventBus);
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(PaleLullabyEntities.MULLAND.get(), MullandEntity.createAttributes().build());
            event.put(PaleLullabyEntities.PALE_MINION.get(), PaleMinionEntity.createAttributes().build());
            event.put(PaleLullabyEntities.FALLEN_BLOOD_HUNTER.get(), FallenBloodHunterEntity.createAttributes().build());
            event.put(PaleLullabyEntities.DRIED_BLOOD_GHOST.get(), DriedBloodGhostEntity.createAttributes().build());
            event.put(PaleLullabyEntities.TOLAND_BAT.get(), TolandBatEntity.createAttributes().build());
            if (PaleLullabyCompat.isIronSpellsLoaded()) {
                IronSpellsOnlyCompat.registerIronSpellAttributes(event);
            } else {
                registerVanillaAttributes(event);
            }
        }

        private static void registerVanillaAttributes(EntityAttributeCreationEvent event) {
            event.put(PaleLullabyEntities.VAMPIRE.get(), VampireEntity.createAttributes().build());
            event.put(PaleLullabyEntities.BLOOD_NOBLE.get(), NobleVampireEntity.createAttributes().build());
            event.put(PaleLullabyEntities.BLOOD_LORD.get(), LordVampireEntity.createAttributes().build());
            event.put(PaleLullabyEntities.BLOOD_HUNTER.get(), BloodHunterEntity.createAttributes(HunterRank.RANK_1).build());
            event.put(PaleLullabyEntities.ADEPT_BLOOD_HUNTER.get(), BloodHunterEntity.createAttributes(HunterRank.RANK_2).build());
            event.put(PaleLullabyEntities.VETERAN_BLOOD_HUNTER.get(), BloodHunterEntity.createAttributes(HunterRank.RANK_3).build());
        }

        @SubscribeEvent
        public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
            event.register(PaleLullabyEntities.VAMPIRE.get(), SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(PaleLullabyEntities.DRIED_BLOOD_GHOST.get(), SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(PaleLullabyEntities.BLOOD_HUNTER.get(), SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(PaleLullabyEntities.ADEPT_BLOOD_HUNTER.get(), SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(PaleLullabyEntities.VETERAN_BLOOD_HUNTER.get(), SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(PaleLullabyEntities.BLOOD_NOBLE.get(), SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(PaleLullabyEntities.BLOOD_LORD.get(), SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }
}
