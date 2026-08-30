package com.littlh.palelullaby;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PaleLullabyFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, PaleLullaby.MOD_ID);


    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> RED_NEEDLE_TREE =
            FEATURES.register("red_needle_tree", () -> new RedNeedleTreeFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> WITHERED_RED_NEEDLE_TREE =
            FEATURES.register("withered_red_needle_tree", () -> new WitheredRedNeedleTreeFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> MINI_OAK_BUSH =
            FEATURES.register("mini_oak_bush", () -> new MiniOakBushFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> BIG_BLOOD_LAKE =
            FEATURES.register("big_blood_lake", () -> new BigBloodLakeFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> THORN_PLANT =
            FEATURES.register("thorn_plant", () -> new ThornPlantFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> WITHERED_SPIRE =
            FEATURES.register("withered_spire", () -> new WitheredSpireFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> WITHERED_MISTLETOE =
            FEATURES.register("withered_mistletoe", () -> new WitheredMistletoeFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> ICE_SHEET =
            FEATURES.register("ice_sheet", () -> new IceSheetFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> GOTHIC_CLIFF =
            FEATURES.register("gothic_cliff", () -> new GothicCliffFeature(NoneFeatureConfiguration.CODEC));


    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> ICE_SPIKE =
            FEATURES.register("ice_spike", () -> new IceSpikeFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> ICE_FREEZE =
            FEATURES.register("ice_freeze", () -> new IceFreezeFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<FossilFeatureConfiguration>> WITHERED_BONE =
            FEATURES.register("withered_bone", () -> new WitheredBoneFeature(FossilFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<SimpleBlockConfiguration>> SAFE_PLANT =
            FEATURES.register("safe_plant", () -> new SafePlantFeature(SimpleBlockConfiguration.CODEC));

}
