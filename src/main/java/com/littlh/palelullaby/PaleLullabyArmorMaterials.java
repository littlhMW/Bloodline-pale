package com.littlh.palelullaby;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 自定义盔甲材料。
 * 设计：皮革/铁/布为初阶升级材质变体（属性介于初阶与中阶之间）；初阶/中阶/高阶按梯度；放逐/堕落仅怪物掉落；审判官/草药学家/苦修者为风味套装。
 */
public class PaleLullabyArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, PaleLullaby.MOD_ID);

    private static Map<ArmorItem.Type, Integer> defense(int helmet, int chest, int legs, int boots) {
        EnumMap<ArmorItem.Type, Integer> map = new EnumMap<>(ArmorItem.Type.class);
        map.put(ArmorItem.Type.HELMET, helmet);
        map.put(ArmorItem.Type.CHESTPLATE, chest);
        map.put(ArmorItem.Type.LEGGINGS, legs);
        map.put(ArmorItem.Type.BOOTS, boots);
        return map;
    }

    private static ArmorMaterial material(String name, int helmet, int chest, int legs, int boots,
                                          int enchant, float toughness, float knockback,
                                          Supplier<Ingredient> repair) {
        return new ArmorMaterial(
                defense(helmet, chest, legs, boots),
                enchant,
                SoundEvents.ARMOR_EQUIP_LEATHER,
                repair,
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, name))),
                toughness,
                knockback
        );
    }

    // ===== 材质变体（初阶升级：属性介于初阶与中阶之间） =====
    public static final Holder<ArmorMaterial> HUNTER_LEATHER = ARMOR_MATERIALS.register("hunter_leather",
            () -> material("hunter_leather", 2, 6, 5, 2, 17, 0.25F, 0.0F, () -> Ingredient.of(Items.LEATHER)));
    public static final Holder<ArmorMaterial> HUNTER_CLOTH = ARMOR_MATERIALS.register("hunter_cloth",
            () -> material("hunter_cloth", 2, 6, 5, 2, 18, 0.25F, 0.0F, () -> Ingredient.of(Items.WHITE_WOOL)));
    public static final Holder<ArmorMaterial> HUNTER_IRON = ARMOR_MATERIALS.register("hunter_iron",
            () -> material("hunter_iron", 2, 6, 5, 2, 16, 0.25F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT)));

    // ===== 初阶（铁为主，基础档） =====
    public static final Holder<ArmorMaterial> HUNTER_NOVICE = ARMOR_MATERIALS.register("hunter_novice",
            () -> material("hunter_novice", 2, 6, 5, 2, 16, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT)));

    // ===== 中阶（更多银和铁） =====
    public static final Holder<ArmorMaterial> HUNTER_ADEPT = ARMOR_MATERIALS.register("hunter_adept",
            () -> material("hunter_adept", 3, 6, 5, 2, 18, 0.5F, 0.0F,
                    () -> Ingredient.of(PaleLullabyItems.SILVER_INGOT.get())));

    // ===== 高阶（更多金，远强于铁，略低于钻石） =====
    public static final Holder<ArmorMaterial> HUNTER_EXALTED = ARMOR_MATERIALS.register("hunter_exalted",
            () -> material("hunter_exalted", 3, 7, 6, 3, 22, 1.5F, 0.1F,
                    () -> Ingredient.of(Items.GOLD_INGOT)));

    // ===== 放逐（脱离教会，中阶与高阶之间） =====
    public static final Holder<ArmorMaterial> HUNTER_EXILED = ARMOR_MATERIALS.register("hunter_exiled",
            () -> material("hunter_exiled", 3, 7, 5, 3, 20, 1.0F, 0.05F,
                    () -> Ingredient.of(PaleLullabyItems.SILVER_INGOT.get())));

    // ===== 堕落（疯掉的破旧中阶） =====
    public static final Holder<ArmorMaterial> HUNTER_FALLEN = ARMOR_MATERIALS.register("hunter_fallen",
            () -> material("hunter_fallen", 2, 6, 4, 2, 10, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT)));

    // ===== 审判官（布衣+少量铁与金，约等于中阶） =====
    public static final Holder<ArmorMaterial> INQUISITOR = ARMOR_MATERIALS.register("inquisitor",
            () -> material("inquisitor", 3, 6, 5, 2, 18, 0.5F, 0.05F, () -> Ingredient.of(Items.IRON_INGOT)));

    // ===== 草药学家（布+厚皮革，高于原版皮革、略差于铁） =====
    public static final Holder<ArmorMaterial> HERBALIST = ARMOR_MATERIALS.register("herbalist",
            () -> material("herbalist", 2, 5, 4, 1, 14, 0.0F, 0.0F, () -> Ingredient.of(Items.LEATHER)));


    // ===== 血族套装（风味装饰套，属性约等于初阶/银） =====
    public static final Holder<ArmorMaterial> SANGUINE = ARMOR_MATERIALS.register("sanguine",
            () -> material("sanguine", 2, 6, 5, 2, 16, 0.0F, 0.0F,
                    () -> Ingredient.of(PaleLullabyItems.SILVER_INGOT.get())));


    // ===== 血族（初级）三变体：革 / 铁 / 金 =====
    public static final Holder<ArmorMaterial> VAMPIRE_LEATHER = ARMOR_MATERIALS.register("vampire_leather",
            () -> material("vampire_leather", 2, 5, 4, 2, 20, 0.0F, 0.0F, () -> Ingredient.of(Items.LEATHER)));
    public static final Holder<ArmorMaterial> VAMPIRE_IRON = ARMOR_MATERIALS.register("vampire_iron",
            () -> material("vampire_iron", 2, 5, 4, 2, 20, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT)));
    public static final Holder<ArmorMaterial> VAMPIRE_GOLD = ARMOR_MATERIALS.register("vampire_gold",
            () -> material("vampire_gold", 2, 5, 4, 2, 20, 0.0F, 0.0F, () -> Ingredient.of(Items.GOLD_INGOT)));

    // ===== 血族贵族 =====
    public static final Holder<ArmorMaterial> BLOOD_NOBLE = ARMOR_MATERIALS.register("blood_noble",
            () -> material("blood_noble", 2, 6, 5, 2, 24, 0.5F, 0.0F,
                    () -> Ingredient.of(PaleLullabyItems.SILVER_INGOT.get())));

    // ===== 血族领主（迷你BOSS级） =====
    public static final Holder<ArmorMaterial> BLOOD_LORD = ARMOR_MATERIALS.register("blood_lord",
            () -> material("blood_lord", 3, 6, 6, 3, 28, 1.0F, 0.05F,
                    () -> Ingredient.of(Items.GOLD_INGOT)));

    // ===== 银（铁与钻石之间） =====
    public static final Holder<ArmorMaterial> SILVER = ARMOR_MATERIALS.register("silver",
            () -> material("silver", 3, 6, 5, 2, 25, 1.0F, 0.05F,
                    () -> Ingredient.of(PaleLullabyItems.SILVER_INGOT.get())));
}
