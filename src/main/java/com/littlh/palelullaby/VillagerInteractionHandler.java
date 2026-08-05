package com.littlh.palelullaby;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = PaleLullaby.MOD_ID)
public class VillagerInteractionHandler {

    private static final String COOLDOWN_KEY = "pale_lullaby:blood_drain_cooldown";
    private static final long COOLDOWN_TICKS = 20 * 60 * 5; // 5 分钟

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.OFF_HAND) return;
        if (!(event.getTarget() instanceof Villager villager)) return;
        if (villager.level().isClientSide()) return;

        Player player = event.getEntity();
        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!offhand.is(Items.GLASS_BOTTLE)) return;

        long current = villager.level().getGameTime();
        long lastDrain = villager.getPersistentData().getLong(COOLDOWN_KEY);
        if (current - lastDrain < COOLDOWN_TICKS) {
            return;
        }

        // 对村民施加虚弱与缓慢
        villager.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 60, 0));
        villager.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 15, 0));
        villager.getPersistentData().putLong(COOLDOWN_KEY, current);

        // 消耗玻璃瓶并给予未提纯血液
        offhand.shrink(1);
        ItemStack blood = new ItemStack(PaleLullabyItems.UNREFINED_BLOOD_BOTTLE.get());
        if (!player.addItem(blood)) {
            player.drop(blood, false);
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
