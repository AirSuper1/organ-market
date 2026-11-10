package com.sale.organ;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganMarket implements ModInitializer {
	public static final String MOD_ID = "organ-market";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModAttachments.init();
		OrganNetworking.register();
		OrganHunger.register();
		OrganPrices.register();
		OrganEffects.register();
		OrganCommands.register();
		LOGGER.info("Organ Market loaded");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
