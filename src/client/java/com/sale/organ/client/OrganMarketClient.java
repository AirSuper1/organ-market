package com.sale.organ.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sale.organ.OrganMarket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public class OrganMarketClient implements ClientModInitializer {

	private static KeyMapping openMarketKey;

	@Override
	public void onInitializeClient() {
		KeyMapping.Category category = KeyMapping.Category.register(OrganMarket.id("main"));

		openMarketKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.organ-market.open_market",
				InputConstants.Type.KEYBOARD,
				InputConstants.KEY_J,
				category
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openMarketKey.consumeClick()) {
				if (client.player != null && client.gui.screen() == null) {
					client.gui.setScreen(new OrganMarketScreen());
				}
			}
		});

		OrganVision.register();

	}
}