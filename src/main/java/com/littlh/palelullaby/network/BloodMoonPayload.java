package com.littlh.palelullaby.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 血月状态同步包：服务端 -> 客户端。 */
public record BloodMoonPayload(boolean active) implements CustomPacketPayload {
    public static final Type<BloodMoonPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("pale_lullaby", "blood_moon"));
    public static final StreamCodec<ByteBuf, BloodMoonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, BloodMoonPayload::active,
            BloodMoonPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
