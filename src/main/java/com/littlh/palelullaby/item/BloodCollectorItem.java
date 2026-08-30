package com.littlh.palelullaby.item;

import net.minecraft.world.effect.MobEffects;
import com.littlh.palelullaby.PaleLullabyItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * 采血器：伤害略微大于空手的武器。
 * 副手持有玻璃瓶时攻击人类（村民/玩家/劫掠者）会将其转换为未提纯血液瓶；
 * 副手持有采血瓶时则转换为血液瓶；被采血的目标获得虚弱效果。
 */
public class BloodCollectorItem extends Item {

    public BloodCollectorItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(256)
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 1.0D, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .build()));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && isHuman(target)) {
            collectBlood(player, target);
        }
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }

    private static boolean isHuman(LivingEntity target) {
        return target instanceof Player
                || target instanceof AbstractVillager
                || target instanceof AbstractIllager;
    }

    private static void collectBlood(Player player, LivingEntity target) {
        ItemStack offhand = player.getOffhandItem();
        Item result = null;
        if (offhand.is(Items.GLASS_BOTTLE)) {
            result = PaleLullabyItems.UNREFINED_BLOOD_BOTTLE.get();
        } else if (offhand.is(PaleLullabyItems.BLOOD_COLLECTION_BOTTLE.get())) {
            result = PaleLullabyItems.BLOOD_BOTTLE.get();
        }
        if (result == null) {
            return;
        }
        ItemStack filled = ItemUtils.createFilledResult(offhand, player, new ItemStack(result));
        player.setItemInHand(InteractionHand.OFF_HAND, filled);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0, false, true, false));
        player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
