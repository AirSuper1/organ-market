package com.sale.organ;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public final class OrganTrade {

    public record Result(boolean success, String message) {
    }

    public static Result sell(ServerPlayer player, Organ organ) {
        List<String> sold = player.getAttachedOrElse(ModAttachments.SOLD_ORGANS, List.of());
        if (sold.contains(organ.getId())) {
            return new Result(false, "Орган уже продан: " + organ.getDisplayName());
        }

        int reward = OrganPrices.reward(player, organ);
        if (freeSpaceForDiamonds(player) < reward) {
            return new Result(false, "Освободи место в инвентаре: нужно место для " + reward + " алмазов.");
        }

        List<String> updated = new ArrayList<>(sold);
        updated.add(organ.getId());
        player.setAttached(ModAttachments.SOLD_ORGANS, updated);
        giveDiamonds(player, reward);
        OrganEffects.sync(player);

        return new Result(true, "Продан орган: " + organ.getDisplayName()
                + ". Получено алмазов: " + reward);
    }

    public static Result buy(ServerPlayer player, Organ organ) {
        List<String> sold = player.getAttachedOrElse(ModAttachments.SOLD_ORGANS, List.of());
        if (!sold.contains(organ.getId())) {
            return new Result(false, "Этот орган у тебя и так на месте: " + organ.getDisplayName());
        }

        int cost = OrganPrices.buyback(player, organ);
        if (countDiamonds(player) < cost) {
            return new Result(false, "Не хватает алмазов. Нужно: " + cost);
        }

        removeDiamonds(player, cost);
        List<String> updated = new ArrayList<>(sold);
        updated.remove(organ.getId());
        player.setAttached(ModAttachments.SOLD_ORGANS, updated);
        OrganEffects.sync(player);

        return new Result(true, "Орган выкуплен: " + organ.getDisplayName()
                + ". Потрачено алмазов: " + cost);
    }

    // Сколько алмазов поместится в основной инвентарь (36 слотов, без брони).
    private static int freeSpaceForDiamonds(ServerPlayer player) {
        Inventory inv = player.getInventory();
        int space = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                space += 64;
            } else if (stack.is(Items.DIAMOND)) {
                space += 64 - stack.getCount();
            }
        }
        return space;
    }

    private static void giveDiamonds(ServerPlayer player, int amount) {
        // Алмазы выдаются стаками, чтобы суммы больше 64 тоже работали.
        int left = amount;
        while (left > 0) {
            int count = Math.min(left, 64);
            player.getInventory().add(new ItemStack(Items.DIAMOND, count));
            left -= count;
        }
    }

    private static int countDiamonds(ServerPlayer player) {
        Inventory inv = player.getInventory();
        int total = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(Items.DIAMOND)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void removeDiamonds(ServerPlayer player, int amount) {
        Inventory inv = player.getInventory();
        int left = amount;
        for (int i = 0; i < inv.getContainerSize() && left > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(Items.DIAMOND)) {
                int take = Math.min(left, stack.getCount());
                stack.shrink(take);
                left -= take;
            }
        }
    }
}