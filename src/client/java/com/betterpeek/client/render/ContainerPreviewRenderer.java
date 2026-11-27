package com.betterpeek.client.render;

import com.betterpeek.client.HudLayoutConfig;
import com.betterpeek.client.PreviewState;
import com.betterpeek.client.detect.WorldContainerDetector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/** Renders the world-container preview only. Inventory shulkers are handled by dedicated tooltip mods. */
public final class ContainerPreviewRenderer {
    private static final int SCREEN_MARGIN = 8;

    private final WorldContainerDetector worldDetector = new WorldContainerDetector();
    private final PreviewGridRenderer gridRenderer = new PreviewGridRenderer();

    public void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        if (!PreviewState.get().enabled()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null || client.currentScreen != null) return;

        PreviewSnapshot snapshot = worldDetector.detect(client);
        if (snapshot == null || snapshot.isEmpty()) return;

        HudLayoutConfig layout = HudLayoutConfig.get();
        float scale = layout.worldScale;
        int panelWidth = Math.round((snapshot.columns() * 16 + 8) * scale);
        int panelHeight = Math.round((snapshot.rows() * 16 + 20) * scale);
        int requestedX = layout.worldX >= 0 ? layout.worldX
            : context.getScaledWindowWidth() - panelWidth - SCREEN_MARGIN;
        int requestedY = layout.worldY >= 0 ? layout.worldY : SCREEN_MARGIN;
        int anchorX = clamp(requestedX, context.getScaledWindowWidth() - panelWidth);
        int anchorY = clamp(requestedY, context.getScaledWindowHeight() - panelHeight);
        drawScaled(context, snapshot, anchorX, anchorY, scale);
    }

    private static int clamp(int value, int screenExtent) {
        return Math.max(0, Math.min(Math.max(0, screenExtent), value));
    }

    private void drawScaled(DrawContext context, PreviewSnapshot snapshot, int x, int y, float scale) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        gridRenderer.render(context, snapshot, 0, 0);
        context.getMatrices().popMatrix();
    }
}
