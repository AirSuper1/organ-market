package com.sale.organ;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;

public final class OrganEffects {

    public static void register() {
        // После возрождения игрок новый, поэтому дебафы вешаем заново.
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> sync(newPlayer));
    }

    // Приводит дебафы игрока в соответствие со списком проданных органов.
    public static void sync(ServerPlayer player) {
        List<String> sold = player.getAttachedOrElse(ModAttachments.SOLD_ORGANS, List.of());

        boolean kidney = sold.contains(Organ.KIDNEY.getId());
        boolean lung = sold.contains(Organ.LUNG.getId());
        boolean eye = sold.contains(Organ.EYE.getId());

        // Почка: -4 здоровья = -2 сердца
        setModifier(player, Attributes.MAX_HEALTH, "kidney_health",
                -4.0, AttributeModifier.Operation.ADD_VALUE, kidney);

        // Лёгкое: скорость -15%
        setModifier(player, Attributes.MOVEMENT_SPEED, "lung_speed",
                -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, lung);

        // Глаз: урон -30%, руки короче
        setModifier(player, Attributes.ATTACK_DAMAGE, "eye_damage",
                -0.30, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, eye);
        setModifier(player, Attributes.BLOCK_INTERACTION_RANGE, "eye_block_range",
                -1.5, AttributeModifier.Operation.ADD_VALUE, eye);
        setModifier(player, Attributes.ENTITY_INTERACTION_RANGE, "eye_entity_range",
                -1.0, AttributeModifier.Operation.ADD_VALUE, eye);
    }

    private static void setModifier(ServerPlayer player, Holder<Attribute> attribute, String name,
                                    double amount, AttributeModifier.Operation operation, boolean active) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        Identifier id = OrganMarket.id(name);
        instance.removeModifier(id); // сначала убираем старый, чтобы не было дубля
        if (active) {
            instance.addPermanentModifier(new AttributeModifier(id, amount, operation));
        }
    }
}