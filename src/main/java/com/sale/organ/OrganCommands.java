package com.sale.organ;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.List;

public final class OrganCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("organ")
                        .then(Commands.literal("list")
                                .executes(ctx -> list(ctx.getSource())))
                        .then(Commands.literal("sell")
                                .then(Commands.argument("organ", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(Organ.values()).map(Organ::getId), builder))
                                        .executes(ctx -> trade(ctx.getSource(), StringArgumentType.getString(ctx, "organ"), true))))
                        .then(Commands.literal("buy")
                                .then(Commands.argument("organ", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(Organ.values()).map(Organ::getId), builder))
                                        .executes(ctx -> trade(ctx.getSource(), StringArgumentType.getString(ctx, "organ"), false))))
                )
        );
    }

    private static int list(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        List<String> sold = player.getAttachedOrElse(ModAttachments.SOLD_ORGANS, List.of());

        source.sendSuccess(() -> Component.literal("Чёрный рынок:").withStyle(ChatFormatting.GOLD), false);
        for (Organ organ : Organ.values()) {
            int reward = OrganPrices.reward(player, organ);
            String line = sold.contains(organ.getId())
                    ? organ.getDisplayName() + " (" + organ.getId() + "): продан, выкуп " + OrganPrices.buybackFor(reward)
                    : organ.getDisplayName() + " (" + organ.getId() + "): на месте, продажа " + reward;
            source.sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }

    private static int trade(CommandSourceStack source, String id, boolean sell) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Organ organ = Organ.fromId(id);
        if (organ == null) {
            source.sendFailure(Component.literal("Нет такого органа. Доступны: stomach, kidney, lung, eye"));
            return 0;
        }

        OrganTrade.Result result = sell ? OrganTrade.sell(player, organ) : OrganTrade.buy(player, organ);
        if (result.success()) {
            source.sendSuccess(() -> Component.literal(result.message()).withStyle(ChatFormatting.GREEN), false);
            return 1;
        }
        source.sendFailure(Component.literal(result.message()));
        return 0;
    }
}