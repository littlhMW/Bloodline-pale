package com.littlh.palelullaby;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * 银工具等级：介于铁与钻石之间。
 */
public enum PaleLullabyToolMaterials implements Tier {
    SILVER(BlockTags.INCORRECT_FOR_IRON_TOOL, 120, 7.0F, 1.5F, 22,
            () -> Ingredient.of(PaleLullabyItems.SILVER_INGOT.get()));

    private final TagKey<Block> incorrectBlocksForDrops;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final java.util.function.Supplier<Ingredient> repairIngredient;

    PaleLullabyToolMaterials(TagKey<Block> incorrectBlocksForDrops, int uses, float speed, float damage,
                             int enchantmentValue, java.util.function.Supplier<Ingredient> repairIngredient) {
        this.incorrectBlocksForDrops = incorrectBlocksForDrops;
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    @Override public int getUses() { return this.uses; }
    @Override public float getSpeed() { return this.speed; }
    @Override public float getAttackDamageBonus() { return this.damage; }
    @Override public TagKey<Block> getIncorrectBlocksForDrops() { return this.incorrectBlocksForDrops; }
    @Override public int getEnchantmentValue() { return this.enchantmentValue; }
    @Override public Ingredient getRepairIngredient() { return this.repairIngredient.get(); }
}
