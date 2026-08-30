package com.littlh.palelullaby;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class IronMaidenBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final int DROP_EVERY_HITS = 6;   // 每造成 6 次伤害判定一次掉落
    private static final float DROP_CHANCE = 0.35F;  // 每次判定 35% 概率掉落

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int trapHits = 0;

    public IronMaidenBlockEntity(BlockPos pos, BlockState state) {
        super(PaleLullabyBlocks.IRON_MAIDEN_BE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> PlayState.CONTINUE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** 内部底座每造成一次伤害调用一次；每隔若干次有概率在内部掉落一枚伤痕印记。 */
    public void onTrapDamage(boolean dropForPlayer) {
        Level level = this.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        this.trapHits++;
        if (this.trapHits < DROP_EVERY_HITS) {
            return;
        }
        this.trapHits = 0;
        if (!dropForPlayer || level.random.nextFloat() >= DROP_CHANCE) {
            return;
        }
        ItemStack stack = new ItemStack(PaleLullabyItems.SCAR_MARK.get());
        ItemEntity item = new ItemEntity(level,
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 1.0,
                this.worldPosition.getZ() + 0.5,
                stack);
        item.setDeltaMovement(
                (level.random.nextDouble() - 0.5) * 0.1,
                0.1,
                (level.random.nextDouble() - 0.5) * 0.1);
        level.addFreshEntity(item);
    }
}
