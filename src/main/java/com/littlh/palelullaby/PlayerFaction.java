package com.littlh.palelullaby;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * 玩家阵营（未来会扩展成完整系统，先提供道具接入所需的最小功能）。
 * 存储于玩家持久数据，重进存档不丢失。
 * 默认中立（偏血猎）：血族攻击玩家，血猎不主动攻击玩家。
 */
public final class PlayerFaction {
    public static final String TAG = "pale_lullaby_faction";

    public enum Faction {
        NEUTRAL("neutral"),
        HUNTER("hunter"),
        VAMPIRE("vampire");

        private final String key;

        Faction(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }

        public static Faction byKey(String key) {
            for (Faction faction : values()) {
                if (faction.key.equals(key)) {
                    return faction;
                }
            }
            return NEUTRAL;
        }
    }

    private PlayerFaction() {
    }

    public static Faction of(Player player) {
        CompoundTag tag = player.getPersistentData();
        return Faction.byKey(tag.getString(TAG));
    }

    public static void set(Player player, Faction faction) {
        player.getPersistentData().putString(TAG, faction.key());
    }
}