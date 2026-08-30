package com.littlh.palelullaby.item;

import com.littlh.palelullaby.BloodMoonManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 少女的血月：夜晚使用召唤全局血月天气，白天拒绝使用。
 */
public class MaidensBloodMoonItem extends Item {
    public MaidensBloodMoonItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (!level.isNight()) {
            player.displayClientMessage(Component.translatable("message.pale_lullaby.blood_moon.day"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.fail(stack);
        }
        BloodMoonManager.start(serverLevel.getServer());
        player.displayClientMessage(Component.translatable("message.pale_lullaby.blood_moon.start"), true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
