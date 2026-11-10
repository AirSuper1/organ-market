package com.sale.organ;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OrganActionPayload(String organId, boolean sell) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OrganActionPayload> TYPE =
            new CustomPacketPayload.Type<>(OrganMarket.id("organ_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OrganActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, OrganActionPayload::organId,
            ByteBufCodecs.BOOL, OrganActionPayload::sell,
            OrganActionPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}