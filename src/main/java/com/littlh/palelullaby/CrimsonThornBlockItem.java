package com.littlh.palelullaby;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

/**
 * 猩红荆棘的物品形态：只允许创造模式放置，
 * 生存模式无法种植（荆棘只能由自然生成和棘果对地面使用催生）。
 */
public class CrimsonThornBlockItem extends BlockItem {
    public CrimsonThornBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            return InteractionResult.PASS;
        }
        return super.useOn(context);
    }
}
