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

/** Full-screen editor for positioning and scaling the world-container preview. */
public final class HudEditorScreen extends Screen {
    private static final int MARGIN = 8;
    private static final int HANDLE = 10;
    private final PreviewGridRenderer renderer = new PreviewGridRenderer();
    private final PreviewSnapshot worldSnapshot;
    private int selected = -1;
    private boolean resizing;
    private double grabX;
    private double grabY;
    private float initialScale;

    public HudEditorScreen(PreviewSnapshot worldSnapshot, PreviewSnapshot ignoredInventorySnapshot) {
        super(Text.literal("BetterPeek HUD Editor"));
        this.worldSnapshot = worldSnapshot != null && !worldSnapshot.isEmpty()
            ? worldSnapshot : placeholder("World container");
    }

    private static PreviewSnapshot placeholder(String name) {
        return new PreviewSnapshot(Text.literal(name), 9, 3,
            Collections.nCopies(27, ItemStack.EMPTY));
    }

    private HudLayoutConfig layout() { return HudLayoutConfig.get(); }

    private int panelWidth(float scale) {
        return Math.round((worldSnapshot.columns() * 16 + 8) * scale);
    }

    private int panelHeight(float scale) {
        return Math.round((worldSnapshot.rows() * 16 + 20) * scale);
    }

    private int x() {
        int panelWidth = panelWidth(layout().worldScale);
        int requested = layout().worldX < 0 ? width - panelWidth - MARGIN : layout().worldX;
        return Math.max(0, Math.min(Math.max(0, width - panelWidth), requested));
    }

    private int y() {
        int panelHeight = panelHeight(layout().worldScale);
        int requested = layout().worldY < 0 ? MARGIN : layout().worldY;
        return Math.max(0, Math.min(Math.max(0, height - panelHeight), requested));
    }

    private void drawPanel(DrawContext context) {
        int px = x();
        int py = y();
        float scale = layout().worldScale;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(px, py);
        context.getMatrices().scale(scale, scale);
        renderer.render(context, worldSnapshot, 0, 0);
        context.getMatrices().popMatrix();
        int w = panelWidth(scale);
        int h = panelHeight(scale);
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
        int px = x(), py = y();
        int w = panelWidth(layout().worldScale);
        int h = panelHeight(layout().worldScale);
        if (mouseX >= px && mouseX <= px + w && mouseY >= py && mouseY <= py + h) {
            selected = 0;
            resizing = mouseX >= px + w - HANDLE && mouseY >= py + h - HANDLE;
            grabX = mouseX - px;
            grabY = mouseY - py;
            initialScale = layout().worldScale;
        } else {
            selected = -1;
        }
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
            layout().worldScale = Math.max(0.5f, Math.min(2.5f, next));
            setPosition(x(), y());
        } else {
            setPosition((int) Math.round(mouseX - grabX), (int) Math.round(mouseY - grabY));
        }
        return true;
    }

    private void setPosition(int px, int py) {
        int maxX = Math.max(0, width - panelWidth(layout().worldScale));
        int maxY = Math.max(0, height - panelHeight(layout().worldScale));
        px = Math.max(0, Math.min(maxX, px));
        py = Math.max(0, Math.min(maxY, py));
        layout().worldX = px;
        layout().worldY = py;
        layout().save();
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && selected >= 0) {
            int px = x(), py = y();
            int w = panelWidth(layout().worldScale);
            int h = panelHeight(layout().worldScale);
            if (px <= MARGIN) px = 0;
            if (py <= MARGIN) py = 0;
            if (width - (px + w) <= MARGIN) px = Math.max(0, width - w);
            if (height - (py + h) <= MARGIN) py = Math.max(0, height - h);
            setPosition(px, py);
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
