package com.littlh.bloodline.entity;

import com.littlh.bloodline.Bloodline;
import com.littlh.bloodline.entity.minion.PaleMinionEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BloodlineEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Bloodline.MOD_ID);

    public static final Supplier<EntityType<KuxuezheEntity>> KUXUEZHE =
            ENTITY_TYPES.register("kuxuezhe",
                    () -> EntityType.Builder.of(KuxuezheEntity::new, MobCategory.MONSTER)
                            .sized(1.4f, 3.5f)
                            .eyeHeight(2.9f)
                            .fireImmune()
                            .clientTrackingRange(64)
                            .build("kuxuezhe"));

    public static final Supplier<EntityType<PaleMinionEntity>> PALE_MINION =
            ENTITY_TYPES.register("pale_minion",
                    () -> EntityType.Builder.of(PaleMinionEntity::new, MobCategory.MONSTER)
                            .sized(0.8f, 1.8f)
                            .eyeHeight(1.5f)
                            .clientTrackingRange(32)
                            .build("pale_minion"));
}
