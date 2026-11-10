package com.sale.organ;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.List;

public final class ModAttachments {
    // Список id проданных органов. Сохраняется в мире, переживает смерть
    // и синхронизируется с клиентом самого игрока.
    public static final AttachmentType<List<String>> SOLD_ORGANS = AttachmentRegistry.<List<String>>create(
            OrganMarket.id("sold_organs"),
            builder -> builder
                    .initializer(() -> List.of())
                    .persistent(Codec.STRING.listOf())
                    .copyOnDeath()
                    .syncWith(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), AttachmentSyncPredicate.targetOnly())
    );

    // Цены продажи органов в порядке Organ.values(). Не сохраняется (считается из сида),
    // только синхронизируется с клиентом, чтобы окно показывало правильные числа.
    public static final AttachmentType<List<Integer>> MARKET_PRICES = AttachmentRegistry.<List<Integer>>create(
            OrganMarket.id("market_prices"),
            builder -> builder
                    .initializer(() -> List.of())
                    .syncWith(ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), AttachmentSyncPredicate.targetOnly())
    );

    public static void init() {
        // При возрождении вручную переносим список проданных органов к новому игроку.
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            List<String> sold = oldPlayer.getAttachedOrElse(SOLD_ORGANS, List.of());
            newPlayer.setAttached(SOLD_ORGANS, sold);
        });
    }
}