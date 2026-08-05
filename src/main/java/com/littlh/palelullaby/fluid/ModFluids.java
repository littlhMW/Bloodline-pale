package com.littlh.palelullaby.fluid;

import com.littlh.palelullaby.PaleLullaby;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids {
    // ==================== DeferredRegisters ====================
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, PaleLullaby.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, PaleLullaby.MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(PaleLullaby.MOD_ID);
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(PaleLullaby.MOD_ID);

    // ==================== Gray Matter Fluid ====================
    public static final DeferredHolder<FluidType, GrayMatterFluidType> GRAY_MATTER_FLUID_TYPE =
            FLUID_TYPES.register("gray_matter", GrayMatterFluidType::new);

    public static final DeferredHolder<Fluid, GrayMatterFluid.Source> GRAY_MATTER_SOURCE =
            FLUIDS.register("gray_matter", () -> new GrayMatterFluid.Source(makeGrayMatterProperties()));

    public static final DeferredHolder<Fluid, GrayMatterFluid.Flowing> GRAY_MATTER_FLOWING =
            FLUIDS.register("flowing_gray_matter", () -> new GrayMatterFluid.Flowing(makeGrayMatterProperties()));

    public static final DeferredHolder<Block, LiquidBlock> GRAY_MATTER_BLOCK =
            BLOCKS.register("gray_matter", () -> new LiquidBlock(GRAY_MATTER_FLOWING.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredHolder<Item, BucketItem> GRAY_MATTER_BUCKET =
            ITEMS.register("gray_matter_bucket", () -> new BucketItem(GRAY_MATTER_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    // ==================== Blood Fluid ====================
    public static final DeferredHolder<FluidType, BloodFluidType> BLOOD_FLUID_TYPE =
            FLUID_TYPES.register("blood", BloodFluidType::new);

    public static final DeferredHolder<Fluid, BloodFluid.Source> BLOOD_SOURCE =
            FLUIDS.register("blood", () -> new BloodFluid.Source(makeBloodProperties()));

    public static final DeferredHolder<Fluid, BloodFluid.Flowing> BLOOD_FLOWING =
            FLUIDS.register("flowing_blood", () -> new BloodFluid.Flowing(makeBloodProperties()));

    public static final DeferredHolder<Block, LiquidBlock> BLOOD_BLOCK =
            BLOCKS.register("blood", () -> new LiquidBlock(BLOOD_FLOWING.get(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_RED)
                            .replaceable()
                            .noCollission()
                            .strength(100.0F)
                            .pushReaction(PushReaction.DESTROY)
                            .noLootTable()
                            .liquid()
                            .sound(SoundType.EMPTY)));

    public static final DeferredHolder<Item, BucketItem> BLOOD_BUCKET =
            ITEMS.register("blood_bucket", () -> new BucketItem(BLOOD_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    // ==================== Fluid Properties ====================
    private static BaseFlowingFluid.Properties makeGrayMatterProperties() {
        return new BaseFlowingFluid.Properties(GRAY_MATTER_FLUID_TYPE, GRAY_MATTER_SOURCE, GRAY_MATTER_FLOWING)
                .block(GRAY_MATTER_BLOCK)
                .bucket(GRAY_MATTER_BUCKET)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(30);
    }

    private static BaseFlowingFluid.Properties makeBloodProperties() {
        return new BaseFlowingFluid.Properties(BLOOD_FLUID_TYPE, BLOOD_SOURCE, BLOOD_FLOWING)
                .block(BLOOD_BLOCK)
                .bucket(BLOOD_BUCKET)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5);
    }

    // ==================== Registration ====================
    public static void register(IEventBus bus) {
        FLUID_TYPES.register(bus);
        FLUIDS.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
