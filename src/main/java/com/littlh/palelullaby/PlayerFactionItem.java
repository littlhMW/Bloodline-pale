package com.littlh.palelullaby;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 改变玩家阵营的一次性道具：金泪滴徽章（血猎）、铁露滴徽章（血族）、
 * 无辜者的舌头（中立）。右键使用后消耗，并同步阵营给相关怪物。
 */
public class PlayerFactionItem extends Item {
    private final PlayerFaction.Faction faction;
    private final String messageKey;

    public PlayerFactionItem(Properties properties, PlayerFaction.Faction faction, String messageKey) {
        super(properties);
        this.faction = faction;
        this.messageKey = messageKey;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            PlayerFaction.set(player, faction);
            player.displayClientMessage(Component.translatable(messageKey), true);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}