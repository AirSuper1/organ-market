package com.sale.organ.client;

import com.sale.organ.ModAttachments;
import com.sale.organ.Organ;
import com.sale.organ.OrganActionPayload;
import com.sale.organ.OrganPrices;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class OrganMarketScreen extends Screen {

    private List<String> shownSold = List.of();
    private List<Integer> shownPrices = List.of();

    public OrganMarketScreen() {
        super(Component.literal("Чёрный рынок"));
    }

    @Override
    protected void init() {
        List<String> sold = currentSold();
        List<Integer> prices = currentPrices();
        this.shownSold = sold;
        this.shownPrices = prices;

        int y = this.height / 2 - 55;
        for (Organ organ : Organ.values()) {
            boolean isSold = sold.contains(organ.getId());
            int reward = rewardOf(prices, organ);
            Component label = isSold
                    ? Component.literal(organ.getDisplayName() + ": выкупить за " + OrganPrices.buybackFor(reward))
                    : Component.literal(organ.getDisplayName() + ": продать за " + reward);

            Button button = Button.builder(label,
                            b -> ClientPlayNetworking.send(new OrganActionPayload(organ.getId(), !isSold)))
                    .bounds(this.width / 2 - 100, y, 200, 20)
                    .build();
            this.addRenderableWidget(button);
            y += 24;
        }

        Button close = Button.builder(Component.literal("Закрыть"), b -> this.onClose())
                .bounds(this.width / 2 - 50, y + 10, 100, 20)
                .build();
        this.addRenderableWidget(close);
    }

    @Override
    public void tick() {
        super.tick();
        // Если сервер прислал новые данные, перерисовываем кнопки.
        if (!currentSold().equals(this.shownSold) || !currentPrices().equals(this.shownPrices)) {
            this.rebuildWidgets();
        }
    }

    // Важно: иначе в одиночной игре сервер встаёт на паузу и не обработает пакет.
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int rewardOf(List<Integer> prices, Organ organ) {
        int index = organ.ordinal();
        return index < prices.size() ? prices.get(index) : organ.getReward();
    }

    private List<String> currentSold() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return List.of();
        }
        return this.minecraft.player.getAttachedOrElse(ModAttachments.SOLD_ORGANS, List.of());
    }

    private List<Integer> currentPrices() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return List.of();
        }
        return this.minecraft.player.getAttachedOrElse(ModAttachments.MARKET_PRICES, List.of());
    }
}