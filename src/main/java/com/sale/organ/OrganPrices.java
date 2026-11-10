package com.sale.organ;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class OrganPrices {

    private static final int MIN_REWARD = 6;
    private static final int MAX_REWARD = 32;
    private static final double BUYBACK_MULTIPLIER = 2.5;

    public static void register() {
        // Тем игрокам, у которых цен ещё нет (вход в мир, возрождение), отправляем их.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.getAttachedOrElse(ModAttachments.MARKET_PRICES, List.of()).isEmpty()) {
                    List<Integer> prices = new ArrayList<>();
                    for (Organ organ : Organ.values()) {
                        prices.add(reward(player, organ));
                    }
                    player.setAttached(ModAttachments.MARKET_PRICES, prices);
                }
            }
        });
    }

    public static int reward(ServerPlayer player, Organ organ) {
        return reward(seedOf(player), organ);
    }

    public static int buyback(ServerPlayer player, Organ organ) {
        return buybackFor(reward(player, organ));
    }

    public static int buybackFor(int reward) {
        return (int) Math.round(reward * BUYBACK_MULTIPLIER);
    }

    private static int reward(long seed, Organ organ) {
        long mixed = seed ^ (organ.getId().hashCode() * 0x9E3779B97F4A7C15L);
        Random random = new Random(mixed);
        random.nextInt(); // прогрев генератора
        return MIN_REWARD + random.nextInt(MAX_REWARD - MIN_REWARD + 1);
    }

    private static long seedOf(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return 0L;
        }
        return server.overworld().getSeed();
    }
}