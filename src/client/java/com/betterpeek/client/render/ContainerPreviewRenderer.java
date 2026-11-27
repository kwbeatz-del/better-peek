package com.betterpeek.client.render;

import com.betterpeek.client.HudLayoutConfig;
import com.betterpeek.client.PreviewState;
import com.betterpeek.client.detect.InventoryShulkerDetector;
import com.betterpeek.client.detect.WorldContainerDetector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderTickCounter;

/** Coordinates the world-container HUD preview and inventory-shulker preview. */
public final class ContainerPreviewRenderer {
    private static final int SCREEN_MARGIN = 8;

    private final WorldContainerDetector worldDetector = new WorldContainerDetector();
    private final InventoryShulkerDetector inventoryDetector = new InventoryShulkerDetector();
    private final PreviewGridRenderer gridRenderer = new PreviewGridRenderer();

    public void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        if (!PreviewState.get().enabled()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null || client.currentScreen != null) return;

        PreviewSnapshot snapshot = worldDetector.detect(client);
        if (snapshot == null || snapshot.isEmpty()) return;

        HudLayoutConfig layout = HudLayoutConfig.get();
        float scale = layout.worldScale;
        int width = snapshot.columns() * 16 + 8;
        int anchorX = layout.worldX >= 0 ? layout.worldX
            : context.getScaledWindowWidth() - Math.round(width * scale) - SCREEN_MARGIN;
        int anchorY = layout.worldY >= 0 ? layout.worldY : SCREEN_MARGIN;
        drawScaled(context, snapshot, anchorX, anchorY, scale);
    }

    public void renderScreen(Screen screen, DrawContext context, int mouseX, int mouseY, float tickDelta) {
        if (!PreviewState.get().enabled()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        PreviewSnapshot snapshot = inventoryDetector.detect(client);
        if (snapshot == null || snapshot.isEmpty()) return;

        HudLayoutConfig layout = HudLayoutConfig.get();
        float scale = layout.inventoryScale;
        int width = snapshot.columns() * 16 + 8;
        int height = snapshot.rows() * 16 + 20;
        int anchorX = layout.inventoryX >= 0 ? layout.inventoryX
            : Math.min(mouseX + 12, context.getScaledWindowWidth() - Math.round(width * scale) - SCREEN_MARGIN);
        int anchorY = layout.inventoryY >= 0 ? layout.inventoryY
            : Math.max(mouseY - Math.round(height * scale) - 4, SCREEN_MARGIN);
        drawScaled(context, snapshot, anchorX, anchorY, scale);
    }

    private void drawScaled(DrawContext context, PreviewSnapshot snapshot, int x, int y, float scale) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        gridRenderer.render(context, snapshot, 0, 0);
        context.getMatrices().popMatrix();
    }
}
