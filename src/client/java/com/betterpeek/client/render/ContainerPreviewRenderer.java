package com.betterpeek.client.render;

import com.betterpeek.client.HudLayoutConfig;
import com.betterpeek.client.PreviewState;
import com.betterpeek.client.detect.WorldContainerDetector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/** Renders the world-container preview only. */
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
        float scale = PreviewSizing.effectiveScale(snapshot, layout.worldScale);
        int panelWidth = PreviewSizing.panelWidth(snapshot, scale);
        int panelHeight = PreviewSizing.panelHeight(snapshot, scale);
        int requestedX = layout.worldX >= 0 ? layout.worldX : context.getScaledWindowWidth() - panelWidth - SCREEN_MARGIN;
        int requestedY = layout.worldY >= 0 ? layout.worldY : SCREEN_MARGIN;
        int x = clamp(requestedX, context.getScaledWindowWidth() - panelWidth);
        int y = clamp(requestedY, context.getScaledWindowHeight() - panelHeight);

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        gridRenderer.render(context, snapshot, 0, 0);
        context.getMatrices().popMatrix();
    }

    private static int clamp(int value, int extent) {
        return Math.max(0, Math.min(Math.max(0, extent), value));
    }
}