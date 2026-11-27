package com.betterpeek.client;

import com.betterpeek.client.render.PreviewGridRenderer;
import com.betterpeek.client.render.PreviewSizing;
import com.betterpeek.client.render.PreviewSnapshot;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.Collections;

public final class HudEditorScreen extends Screen {
    private static final int MARGIN = 8, HANDLE = 10;
    private final PreviewGridRenderer renderer = new PreviewGridRenderer();
    private final PreviewSnapshot worldSnapshot;
    private int selected = -1;
    private boolean resizing;
    private double grabX, grabY;

    public HudEditorScreen(PreviewSnapshot worldSnapshot, PreviewSnapshot ignoredInventorySnapshot) {
        super(Text.literal("BetterPeek HUD Editor"));
        this.worldSnapshot = worldSnapshot != null && !worldSnapshot.isEmpty()
            ? worldSnapshot : new PreviewSnapshot(Text.literal("World container"), 9, 6,
                Collections.nCopies(54, ItemStack.EMPTY));
    }

    private HudLayoutConfig layout() { return HudLayoutConfig.get(); }
    private float scale() { return PreviewSizing.effectiveScale(worldSnapshot, layout().worldScale); }
    private int panelWidth() { return PreviewSizing.panelWidth(worldSnapshot, scale()); }
    private int panelHeight() { return PreviewSizing.panelHeight(worldSnapshot, scale()); }
    private int x() {
        int requested = layout().worldX < 0 ? width - panelWidth() - MARGIN : layout().worldX;
        return Math.max(0, Math.min(Math.max(0, width - panelWidth()), requested));
    }
    private int y() {
        int requested = layout().worldY < 0 ? MARGIN : layout().worldY;
        return Math.max(0, Math.min(Math.max(0, height - panelHeight()), requested));
    }

    private void drawPanel(DrawContext context) {
        int px = x(), py = y();
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(px, py);
        context.getMatrices().scale(scale(), scale());
        renderer.render(context, worldSnapshot, 0, 0);
        context.getMatrices().popMatrix();
        int w = panelWidth(), h = panelHeight();
        context.drawStrokedRectangle(px, py, w, h, selected == 0 ? 0xFFFFFF55 : 0xFF70A0FF);
        context.fill(px + w - HANDLE, py + h - HANDLE, px + w, py + h,
            selected == 0 ? 0xFFFFFF55 : 0xFF70A0FF);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xB0000000);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Drag the panel to move it • Drag its bottom-right handle to resize"),
            width / 2, 28, 0xFFE0E0E0);
        drawPanel(context);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("R: Reset layout     Esc: Close and save"), width / 2, height - 18, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return super.mouseClicked(click, doubled);
        double mx = click.x(), my = click.y();
        int px = x(), py = y(), w = panelWidth(), h = panelHeight();
        if (mx >= px && mx <= px + w && my >= py && my <= py + h) {
            selected = 0;
            resizing = mx >= px + w - HANDLE && my >= py + h - HANDLE;
            grabX = mx - px;
            grabY = my - py;
        } else selected = -1;
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (selected < 0 || click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT)
            return super.mouseDragged(click, offsetX, offsetY);

        if (resizing) {
            // The drag offsets are incremental per event; accumulate for smooth resizing.
            float delta = (float) Math.max(offsetX, offsetY) / 180.0f;
            layout().worldScale = Math.max(0.35f, Math.min(2.5f, layout().worldScale + delta));
            setPosition(x(), y());
        } else {
            setPosition((int)Math.round(click.x() + offsetX - grabX),
                        (int)Math.round(click.y() + offsetY - grabY));
        }
        return true;
    }

    private void setPosition(int px, int py) {
        int maxX = Math.max(0, width - panelWidth()), maxY = Math.max(0, height - panelHeight());
        layout().worldX = Math.max(0, Math.min(maxX, px));
        layout().worldY = Math.max(0, Math.min(maxY, py));
        layout().save();
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && selected >= 0) {
            int px = x(), py = y(), w = panelWidth(), h = panelHeight();
            if (px <= MARGIN) px = 0;
            if (py <= MARGIN) py = 0;
            if (width - (px + w) <= MARGIN) px = Math.max(0, width - w);
            if (height - (py + h) <= MARGIN) py = Math.max(0, height - h);
            setPosition(px, py);
            selected = -1; resizing = false; layout().save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_R) { layout().reset(); return true; }
        return super.keyPressed(input);
    }

    @Override
    public void close() { layout().save(); if (client != null) client.setScreen(null); }
    @Override
    public boolean shouldPause() { return false; }
}