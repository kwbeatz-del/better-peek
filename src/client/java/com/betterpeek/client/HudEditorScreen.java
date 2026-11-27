package com.betterpeek.client;

import com.betterpeek.client.render.PreviewGridRenderer;
import com.betterpeek.client.render.PreviewSnapshot;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;

/** Full-screen editor for independently positioning and scaling both previews. */
public final class HudEditorScreen extends Screen {
    private static final int MARGIN = 8;
    private static final int HANDLE = 10;
    private final PreviewGridRenderer renderer = new PreviewGridRenderer();
    private final PreviewSnapshot worldSnapshot;
    private final PreviewSnapshot inventorySnapshot;
    private int selected = -1;
    private boolean resizing;
    private double grabX;
    private double grabY;
    private float initialScale;

    public HudEditorScreen(PreviewSnapshot worldSnapshot, PreviewSnapshot inventorySnapshot) {
        super(Text.literal("BetterPeek HUD Editor"));
        this.worldSnapshot = worldSnapshot != null && !worldSnapshot.isEmpty()
            ? worldSnapshot : placeholder("World container");
        this.inventorySnapshot = inventorySnapshot != null && !inventorySnapshot.isEmpty()
            ? inventorySnapshot : placeholder("Inventory shulker");
    }

    private static PreviewSnapshot placeholder(String name) {
        return new PreviewSnapshot(Text.literal(name), 9, 3,
            Collections.nCopies(27, ItemStack.EMPTY));
    }

    private HudLayoutConfig layout() { return HudLayoutConfig.get(); }

    private int panelWidth(PreviewSnapshot snapshot, float scale) {
        return Math.round((snapshot.columns() * 16 + 8) * scale);
    }

    private int panelHeight(PreviewSnapshot snapshot, float scale) {
        return Math.round((snapshot.rows() * 16 + 20) * scale);
    }

    private int x(int panel) {
        PreviewSnapshot snapshot = snapshot(panel);
        int panelWidth = panelWidth(snapshot, scale(panel));
        int requested = panel == 0
            ? (layout().worldX < 0 ? width - panelWidth - MARGIN : layout().worldX)
            : (layout().inventoryX < 0 ? MARGIN : layout().inventoryX);
        return Math.max(0, Math.min(Math.max(0, width - panelWidth), requested));
    }

    private int y(int panel) {
        PreviewSnapshot snapshot = snapshot(panel);
        int panelHeight = panelHeight(snapshot, scale(panel));
        int requested = panel == 0
            ? (layout().worldY < 0 ? MARGIN : layout().worldY)
            : (layout().inventoryY < 0 ? height - panelHeight - MARGIN : layout().inventoryY);
        return Math.max(0, Math.min(Math.max(0, height - panelHeight), requested));
    }

    private float scale(int panel) {
        return panel == 0 ? layout().worldScale : layout().inventoryScale;
    }

    private PreviewSnapshot snapshot(int panel) {
        return panel == 0 ? worldSnapshot : inventorySnapshot;
    }

    private void drawPanel(DrawContext context, int panel) {
        PreviewSnapshot snapshot = snapshot(panel);
        int px = x(panel);
        int py = y(panel);
        float scale = scale(panel);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(px, py);
        context.getMatrices().scale(scale, scale);
        renderer.render(context, snapshot, 0, 0);
        context.getMatrices().popMatrix();
        int w = panelWidth(snapshot, scale);
        int h = panelHeight(snapshot, scale);
        context.drawStrokedRectangle(px, py, w, h, selected == panel ? 0xFFFFFF55 : 0xFF70A0FF);
        context.fill(px + w - HANDLE, py + h - HANDLE, px + w, py + h,
            selected == panel ? 0xFFFFFF55 : 0xFF70A0FF);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xB0000000);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Drag a panel to move it • Drag its bottom-right handle to resize"),
            width / 2, 28, 0xFFE0E0E0);
        drawPanel(context, 0);
        drawPanel(context, 1);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("R: Reset layout     Esc: Close and save"),
            width / 2, height - 18, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(click, doubled);
        }
        double mouseX = click.x();
        double mouseY = click.y();
        for (int panel = 1; panel >= 0; panel--) {
            int px = x(panel), py = y(panel);
            int w = panelWidth(snapshot(panel), scale(panel));
            int h = panelHeight(snapshot(panel), scale(panel));
            if (mouseX >= px && mouseX <= px + w && mouseY >= py && mouseY <= py + h) {
                selected = panel;
                resizing = mouseX >= px + w - HANDLE && mouseY >= py + h - HANDLE;
                grabX = mouseX - px;
                grabY = mouseY - py;
                initialScale = scale(panel);
                return true;
            }
        }
        selected = -1;
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (selected < 0 || click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseDragged(click, offsetX, offsetY);
        }

        double mouseX = click.x() + offsetX;
        double mouseY = click.y() + offsetY;
        if (resizing) {
            float next = initialScale + (float) Math.max(offsetX, offsetY) / 180.0f;
            next = Math.max(0.5f, Math.min(2.5f, next));
            if (selected == 0) layout().worldScale = next;
            else layout().inventoryScale = next;
            setPosition(selected, x(selected), y(selected));
        } else {
            setPosition(selected, (int) Math.round(mouseX - grabX), (int) Math.round(mouseY - grabY));
        }
        return true;
    }

    private void setPosition(int panel, int px, int py) {
        int maxX = Math.max(0, width - panelWidth(snapshot(panel), scale(panel)));
        int maxY = Math.max(0, height - panelHeight(snapshot(panel), scale(panel)));
        px = Math.max(0, Math.min(maxX, px));
        py = Math.max(0, Math.min(maxY, py));
        if (panel == 0) { layout().worldX = px; layout().worldY = py; }
        else { layout().inventoryX = px; layout().inventoryY = py; }
        layout().save();
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && selected >= 0) {
            int px = x(selected), py = y(selected);
            int w = panelWidth(snapshot(selected), scale(selected));
            int h = panelHeight(snapshot(selected), scale(selected));
            if (px <= MARGIN) px = 0;
            if (py <= MARGIN) py = 0;
            if (width - (px + w) <= MARGIN) px = Math.max(0, width - w);
            if (height - (py + h) <= MARGIN) py = Math.max(0, height - h);
            setPosition(selected, px, py);
            selected = -1;
            resizing = false;
            layout().save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_R) {
            layout().reset();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        layout().save();
        if (client != null) client.setScreen(null);
    }

    @Override
    public boolean shouldPause() { return false; }
}
