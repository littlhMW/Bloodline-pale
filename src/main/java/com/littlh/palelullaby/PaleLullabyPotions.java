package com.littlh.palelullaby;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 自定义药水（酿造配方在 PaleLullabyForgeEvents#onRegisterBrewingRecipes 注册）。 */
public class PaleLullabyPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, PaleLullaby.MOD_ID);

    // 中间产物（第一步酿造，效果较短）
    public static final DeferredHolder<Potion, Potion> MIST_ESSENCE = POTIONS.register("mist_essence",
            () -> new Potion(new MobEffectInstance(PaleLullabyEffects.REQUIEM, 20 * 45)));
    public static final DeferredHolder<Potion, Potion> ORCHID_ESSENCE = POTIONS.register("orchid_essence",
            () -> new Potion(new MobEffectInstance(PaleLullabyEffects.GHOST_WALK, 20 * 45)));
    public static final DeferredHolder<Potion, Potion> THORN_ESSENCE = POTIONS.register("thorn_essence",
            () -> new Potion(new MobEffectInstance(PaleLullabyEffects.BLOOD_AURA, 20 * 45)));
    public static final DeferredHolder<Potion, Potion> ROSE_ESSENCE = POTIONS.register("rose_essence",
            () -> new Potion(new MobEffectInstance(PaleLullabyEffects.ROSE_NECTAR, 20 * 45)));
    public static final DeferredHolder<Potion, Potion> ROT_ESSENCE = POTIONS.register("rot_essence",
            () -> new Potion(new MobEffectInstance(PaleLullabyEffects.BLOOD_ROT, 20 * 45)));
    public static final DeferredHolder<Potion, Potion> BERRY_ESSENCE = POTIONS.register("berry_essence",
            () -> new Potion(new MobEffectInstance(PaleLullabyEffects.THORN_SKIN, 20 * 45)));

    // 成品
    public static final DeferredHolder<Potion, Potion> MIST_REQUIEM = POTIONS.register("mist_requiem",
            () -> new Potion(new MobEffectInstance(PaleLullabyEffects.REQUIEM, 20 * 180)));
    public static final DeferredHolder<Potion, Potion> GHOST_ORCHID_DEW = POTIONS.register("ghost_orchid_dew",
            () -> new Potion(new MobEffectInstance(PaleLullabyEffects.GHOST_WALK, 20 * 180)));
    public static final DeferredHolder<Potion, Potion> BLOOD_THORN_TOUCH = POTIONS.register("blood_thorn_touch",
            () -> new Potion(new MobEffectInstance(PaleLullabyEffects.BLOOD_AURA, 20 * 180)));
    public static final DeferredHolder<Potion, Potion> ROSE_MEAD = POTIONS.register("rose_mead",
            () -> new Potion(new MobEffectInstance(PaleLullabyEffects.ROSE_NECTAR, 20 * 180)));
    public static final DeferredHolder<Potion, Potion> ROTHEART_DEW = POTIONS.register("rotheart_dew",
            () -> new Potion(new MobEffectInstance(PaleLullabyEffects.BLOOD_ROT, 20 * 90)));
    public static final DeferredHolder<Potion, Potion> THORN_BERRY_BREW = POTIONS.register("thorn_berry_brew",
            () -> new Potion(new MobEffectInstance(PaleLullabyEffects.THORN_SKIN, 20 * 120)));
    // 凋萎槲寄生系：麻痹 / 假死
    public static final DeferredHolder<Potion, Potion> WITHERED_ESSENCE = POTIONS.register("withered_essence",
            () -> new Potion(new MobEffectInstance(PaleLullabyEffects.PARALYSIS, 20 * 45)));
    public static final DeferredHolder<Potion, Potion> WEAKNESS_OIL = POTIONS.register("weakness_oil",
            () -> new Potion(new MobEffectInstance(PaleLullabyEffects.PARALYSIS, 20 * 150, 1)));
    public static final DeferredHolder<Potion, Potion> FEIGNED_DEATH = POTIONS.register("feigned_death",
            () -> new Potion(
                    new MobEffectInstance(PaleLullabyEffects.FEIGNED_DEATH, 20 * 150),
                    new MobEffectInstance(PaleLullabyEffects.PARALYSIS, 20 * 150)));
}
