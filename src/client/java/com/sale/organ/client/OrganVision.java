package com.sale.organ.client;

import com.sale.organ.ModAttachments;
import com.sale.organ.Organ;
import com.sale.organ.OrganMarket;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public final class OrganVision {

    private static final int STEPS = 24;        // сколько слоёв в затемнении
    private static final int MAX_ALPHA = 235;   // непрозрачность у самого края (0-255)
    private static final float DEPTH = 0.38F;   // насколько вглубь экрана заходит тьма

    public static void register() {
        // Рисуем до прицела и панели предметов, чтобы интерфейс оставался читаемым.
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CROSSHAIR,
                OrganMarket.id("eye_tunnel"),
                OrganVision::extract
        );
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        List<String> sold = mc.player.getAttachedOrElse(ModAttachments.SOLD_ORGANS, List.of());
        if (!sold.contains(Organ.EYE.getId())) {
            return;
        }

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        int step = Math.max(1, (int) (Math.min(w, h) * DEPTH / STEPS));

        for (int i = 0; i < STEPS; i++) {
            float t = i / (float) STEPS;
            int alpha = (int) (MAX_ALPHA * Math.pow(1.0F - t, 1.5));
            if (alpha <= 0) {
                break;
            }
            int color = alpha << 24; // чёрный цвет с прозрачностью alpha

            int inset = i * step;
            int x1 = inset;
            int y1 = inset;
            int x2 = w - inset;
            int y2 = h - inset;
            if (x2 - x1 <= 2 * step || y2 - y1 <= 2 * step) {
                break;
            }

            graphics.fill(x1, y1, x2, y1 + step, color);                 // верх
            graphics.fill(x1, y2 - step, x2, y2, color);                 // низ
            graphics.fill(x1, y1 + step, x1 + step, y2 - step, color);   // лево
            graphics.fill(x2 - step, y1 + step, x2, y2 - step, color);   // право
        }
    }
}