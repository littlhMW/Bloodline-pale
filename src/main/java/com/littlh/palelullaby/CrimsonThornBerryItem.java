package com.littlh.palelullaby;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 猩红棘果：对猩红荆棘使用会在该面结出一簇棘果；
 * 对可支撑地面使用则种出一株猩红荆棘（幼苗随随机刻向上生长）。
 */
public class CrimsonThornBerryItem extends Item {
    public CrimsonThornBerryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        BlockPos placePos = null;
        BlockState placeState = null;
        if (clickedState.is(PaleLullabyBlocks.CRIMSON_THORN.get())) {
            Direction face = context.getClickedFace();
            BlockPos target = clickedPos.relative(face);
            if (level.isEmptyBlock(target)) {
                placePos = target;
                placeState = PaleLullabyBlocks.CRIMSON_THORN_BERRY_BLOCK.get().defaultBlockState()
                        .setValue(CrimsonThornAttachmentBlock.FACING, face.getOpposite());
            }
        } else if (isThornSupport(clickedState)) {
            // 生存模式不允许种荆棘，仅创造模式可放置
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                return InteractionResult.PASS;
            }
            BlockPos target = clickedPos.above();
            if (level.isEmptyBlock(target)) {
                placePos = target;
                placeState = CrimsonThornBlock.getStateWithConnections(level, target,
                        PaleLullabyBlocks.CRIMSON_THORN.get().defaultBlockState());
            }
        }
        if (placePos == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.setBlock(placePos, placeState, 3);
            level.playSound(null, placePos, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
            ItemStack stack = context.getItemInHand();
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** 猩红棘果只能种在浸润淤泥系列方块上。 */
    private static boolean isThornSupport(BlockState state) {
        return state.is(PaleLullabyBlocks.SOAKED_MUD.get())
                || state.is(PaleLullabyBlocks.SOAKED_MUD_GRASS.get());
    }
}
