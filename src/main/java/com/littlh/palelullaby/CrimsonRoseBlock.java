package com.littlh.palelullaby;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Crimson rose: grows on crimson thorn and emits slowly falling self-lit sparks. */
public class CrimsonRoseBlock extends CrimsonThornAttachmentBlock {
    /** 是否有蜜：有蜜时发光并掉落粒子；被空瓶采过后变 false。 */
    /** 自然生成的蔷薇：1/3 概率有蜜（发光/粒子/可采蜜），其余无蜜。 */
    public static BlockState naturalState(BlockState state, RandomSource random) {
        return state.setValue(HAS_NECTAR, random.nextFloat() < 0.3333F);
    }
    public static final BooleanProperty HAS_NECTAR = BooleanProperty.create("has_nectar");

    public CrimsonRoseBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HAS_NECTAR, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_NECTAR);
    }

    /** ???????????????? */
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(HAS_NECTAR)) {
            return;
        }
        if (random.nextInt(4) != 0) {
            return;
        }
        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D;
        double y = pos.getY() + 0.5D + random.nextDouble() * 0.4D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D;
        level.addParticle(PaleLullabyParticles.CRIMSON_ROSE_SPARK.get(), x, y, z, 0.0D, 0.0D, 0.0D);
        if (random.nextInt(3) == 0) {
            level.addParticle(PaleLullabyParticles.CRIMSON_ROSE_SPARK.get(),
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D,
                    pos.getY() + 0.4D + random.nextDouble() * 0.5D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D,
                    0.0D, 0.0D, 0.0D);
        }
    }

    /** 采过的花一段时间后变成果子。 */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(HAS_NECTAR) && random.nextInt(200) == 0) {
            level.setBlock(pos, PaleLullabyBlocks.CRIMSON_THORN_BERRY_BLOCK.get().defaultBlockState()
                    .setValue(CrimsonThornAttachmentBlock.FACING, state.getValue(FACING)), 3);
        }
    }

    /** 空瓶右键采蜜；采过的花再右键只会冒出失败粒子。 */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(Items.GLASS_BOTTLE)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (state.getValue(HAS_NECTAR)) {
            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                ItemStack nectar = new ItemStack(PaleLullabyItems.CRIMSON_ROSE_NECTAR.get());
                if (!player.addItem(nectar)) {
                    player.drop(nectar, false);
                }
                level.setBlock(pos, state.setValue(HAS_NECTAR, false), 3);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.isClientSide) {
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 0.6;
            double cz = pos.getZ() + 0.5;
            for (int i = 0; i < 6; i++) {
                level.addParticle(ParticleTypes.SMOKE,
                        cx + (level.random.nextDouble() - 0.5) * 0.4,
                        cy + (level.random.nextDouble() - 0.5) * 0.4,
                        cz + (level.random.nextDouble() - 0.5) * 0.4,
                        (level.random.nextDouble() - 0.5) * 0.05,
                        0.1,
                        (level.random.nextDouble() - 0.5) * 0.05);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
