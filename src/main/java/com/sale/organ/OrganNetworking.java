package com.sale.organ;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class OrganNetworking {

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(OrganActionPayload.TYPE, OrganActionPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(OrganActionPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            Organ organ = Organ.fromId(payload.organId());
            if (organ == null) {
                return;
            }
            OrganTrade.Result result = payload.sell()
                    ? OrganTrade.sell(player, organ)
                    : OrganTrade.buy(player, organ);
            player.sendSystemMessage(Component.literal(result.message())
                    .withStyle(result.success() ? ChatFormatting.GREEN : ChatFormatting.RED));
        });
    }
}