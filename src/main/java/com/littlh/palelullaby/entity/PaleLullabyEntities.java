package com.littlh.palelullaby.entity;

import com.littlh.palelullaby.PaleLullaby;
import com.littlh.palelullaby.entity.minion.PaleMinionEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PaleLullabyEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, PaleLullaby.MOD_ID);

    public static final Supplier<EntityType<MullandEntity>> MULLAND =
            ENTITY_TYPES.register("mulland",
                    () -> EntityType.Builder.of(MullandEntity::new, MobCategory.MONSTER)
                            .sized(1.4f, 3.5f)
                            .eyeHeight(2.9f)
                            .fireImmune()
                            .clientTrackingRange(64)
                            .build("mulland"));

    public static final Supplier<EntityType<PaleMinionEntity>> PALE_MINION =
            ENTITY_TYPES.register("pale_minion",
                    () -> EntityType.Builder.of(PaleMinionEntity::new, MobCategory.MONSTER)
                            .sized(0.8f, 1.8f)
                            .eyeHeight(1.5f)
                            .clientTrackingRange(32)
                            .build("pale_minion"));
}
