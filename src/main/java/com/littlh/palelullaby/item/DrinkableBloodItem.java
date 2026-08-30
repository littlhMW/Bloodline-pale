package com.littlh.palelullaby.item;

import com.littlh.palelullaby.BloodThirstEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * 血液系列物品：可像药水一样按住饮用。
 * 不再提供对猩红荆棘的催熟功能。
 */
public class DrinkableBloodItem extends Item {

    public DrinkableBloodItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide()) {
            entity.playSound(SoundEvents.GENERIC_DRINK, 0.5F, entity.getRandom().nextFloat() * 0.1F + 0.9F);
            if (!(entity instanceof Player player && player.getAbilities().instabuild)) {
                stack.shrink(1);
            }
            // 喝下血液：有概率获得渴血；已有渴血则暂时抑制症状并可能提升等级
            if (entity instanceof ServerPlayer player) {
                BloodThirstEffect.onDrinkBlood(player);
            }
        }
        return stack;
    }
}
