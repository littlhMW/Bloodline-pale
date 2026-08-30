package com.littlh.palelullaby.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 渴血症状抑制同步包：服务端 -> 客户端（玩家实体ID + 抑制结束的游戏时间）。 */
public record BloodThirstSuppressPayload(int playerId, long untilGameTime) implements CustomPacketPayload {
    public static final Type<BloodThirstSuppressPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("pale_lullaby", "blood_thirst_suppress"));
    public static final StreamCodec<ByteBuf, BloodThirstSuppressPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BloodThirstSuppressPayload::playerId,
            ByteBufCodecs.VAR_LONG, BloodThirstSuppressPayload::untilGameTime,
            BloodThirstSuppressPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
