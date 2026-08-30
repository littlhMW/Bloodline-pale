package com.littlh.palelullaby.entity;

import com.littlh.palelullaby.PaleLullabyItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 血猎外观装备池：按阶级给血猎穿对应的猎人套装。
 * 自然生成用固定阶级外观（equip）；营地生成用 equipRandomized，
 * 在阶级允许的装备池内随机抽一套，配合皮肤库让每只血猎外观都不一样。
 */
public final class HunterEquipment {

    private static final Item[][] TIERS = {
            { PaleLullabyItems.CLOTH_HUNTER_HIGH_HAT.get(), PaleLullabyItems.CLOTH_HUNTER_JACKET.get(),
                    PaleLullabyItems.CLOTH_HUNTER_TROUSERS.get(), PaleLullabyItems.CLOTH_HUNTER_ANKLE_BOOTS.get() },
            { PaleLullabyItems.LEATHER_HUNTER_HIGH_HAT.get(), PaleLullabyItems.LEATHER_HUNTER_JACKET.get(),
                    PaleLullabyItems.LEATHER_HUNTER_TROUSERS.get(), PaleLullabyItems.LEATHER_HUNTER_BOOTS.get() },
            { PaleLullabyItems.NOVICE_HUNTER_HIGH_HAT.get(), PaleLullabyItems.NOVICE_HUNTER_JACKET.get(),
                    PaleLullabyItems.NOVICE_HUNTER_TROUSERS.get(), PaleLullabyItems.NOVICE_HUNTER_BOOTS.get() },
            { PaleLullabyItems.ADEPT_HUNTER_HIGH_HAT.get(), PaleLullabyItems.ADEPT_HUNTER_JACKET.get(),
                    PaleLullabyItems.ADEPT_HUNTER_TROUSERS.get(), PaleLullabyItems.ADEPT_HUNTER_BOOTS.get() },
            { PaleLullabyItems.IRON_HUNTER_HIGH_HAT.get(), PaleLullabyItems.IRON_HUNTER_JACKET.get(),
                    PaleLullabyItems.IRON_HUNTER_TROUSERS.get(), PaleLullabyItems.IRON_HUNTER_BOOTS.get() }
    };

    private HunterEquipment() {
    }

    /** 阶级固定外观（自然生成）：初阶布装 / 中阶进阶装 / 高阶铁装。 */
    public static void equip(Mob mob, HunterRank rank) {
        int tier = switch (rank) {
            case RANK_1 -> 0;
            case RANK_2 -> 3;
            case RANK_3 -> 4;
        };
        apply(mob, tier);
    }

    /** 营地随机外观：在该阶级允许的装备池里随机抽一套（按血猎库）。 */
    public static void equipRandomized(Mob mob, HunterRank rank) {
        int tier = switch (rank) {
            case RANK_1 -> 0 + mob.getRandom().nextInt(3); // 布 / 皮 / 入门
            case RANK_2 -> 2 + mob.getRandom().nextInt(3); // 入门 / 进阶 / 铁
            case RANK_3 -> 3 + mob.getRandom().nextInt(2); // 进阶 / 铁
        };
        apply(mob, tier);
    }

    private static void apply(Mob mob, int tier) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            mob.setItemSlot(slot, new ItemStack(TIERS[tier][slotIndex(slot)]));
        }
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(PaleLullabyItems.SILVER_SWORD.get()));
        mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.CROSSBOW));
    }

    private static int slotIndex(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            default -> throw new IllegalArgumentException("Not an armor slot: " + slot);
        };
    }
}
