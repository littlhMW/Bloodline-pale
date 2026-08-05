package com.littlh.palelullaby.item;

import com.littlh.palelullaby.PaleLullabyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;

public class PaleRegretMixtureItem extends Item {

    public PaleRegretMixtureItem() {
        super(new Item.Properties().stacksTo(16).food(new FoodProperties.Builder().alwaysEdible().build()));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // 模拟药水饮用：需要按住使用
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity) {
        if (level.isClientSide()) {
            return stack;
        }
        if (entity instanceof ServerPlayer player) {
            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("pale_lullaby", "pale_cradle"));
            ServerLevel destLevel = player.server.getLevel(dimensionKey);
            if (destLevel != null) {
                // 选择一个平坦地表位置 (0, 附近)
                BlockPos spawnPos = findSafeSpawn(destLevel, 0, 0);
                player.teleportTo(destLevel, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYRot(), player.getXRot());
                // 播放音效
                destLevel.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.5f, 1.0f);
            }
        }
        // 消耗物品（如果不是创造模式）
        if (!(entity instanceof Player p && p.getAbilities().instabuild)) {
            stack.shrink(1);
        }
        return stack;
    }

    /**
     * 在目标维度的指定区块附近寻找安全地表位置
     */
    private static BlockPos findSafeSpawn(ServerLevel world, int chunkX, int chunkZ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // 从 y=128 向下扫描，找到第一个实体可以站立的方块上方
        for (int y = 128; y > world.getMinBuildHeight(); y--) {
            pos.set(chunkX * 16 + 8, y, chunkZ * 16 + 8);
            if (!world.getBlockState(pos).isAir() && world.getBlockState(pos.above()).isAir() && world.getBlockState(pos.above(2)).isAir()) {
                return pos.above();
            }
        }
        // 保底返回 (0, 64, 0) 的空气位置
        return new BlockPos(chunkX * 16 + 8, 64, chunkZ * 16 + 8);
    }
}
