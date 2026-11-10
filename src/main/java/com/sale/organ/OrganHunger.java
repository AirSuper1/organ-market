package com.sale.organ;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class OrganHunger {

    // Дополнительная усталость за тик бега при проданном лёгком.
    // Обычный бег тратит примерно 0.03 за тик, так что это удваивает расход.
    private static final float LUNG_SPRINT_EXHAUSTION = 0.03F;

    private static final Map<UUID, Integer> lastFood = new HashMap<>();
    private static final Map<UUID, Float> lastSaturation = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tickPlayer(player);
            }
        });

        // После возрождения голод сбрасывается, поэтому прошлые значения забываем.
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            lastFood.remove(newPlayer.getUUID());
            lastSaturation.remove(newPlayer.getUUID());
        });
    }

    private static void tickPlayer(ServerPlayer player) {
        List<String> sold = player.getAttachedOrElse(ModAttachments.SOLD_ORGANS, List.of());
        FoodData food = player.getFoodData();
        UUID id = player.getUUID();

        // Лёгкое: на бегу голод тратится быстрее.
        if (sold.contains(Organ.LUNG.getId()) && player.isSprinting()) {
            food.addExhaustion(LUNG_SPRINT_EXHAUSTION);
        }

        int foodNow = food.getFoodLevel();
        float satNow = food.getSaturationLevel();

        Integer foodBefore = lastFood.get(id);
        Float satBefore = lastSaturation.get(id);

        // Желудок: если еды стало больше, половину прироста забираем обратно.
        if (sold.contains(Organ.STOMACH.getId()) && foodBefore != null && satBefore != null) {
            if (foodNow > foodBefore) {
                int gained = foodNow - foodBefore;
                foodNow = foodBefore + (gained + 1) / 2; // округляем вверх: 1 -> 1, 2 -> 1, 3 -> 2
                food.setFoodLevel(foodNow);
            }
            if (satNow > satBefore) {
                satNow = satBefore + (satNow - satBefore) / 2;
                food.setSaturation(satNow);
            }
        }

        lastFood.put(id, foodNow);
        lastSaturation.put(id, satNow);
    }
}