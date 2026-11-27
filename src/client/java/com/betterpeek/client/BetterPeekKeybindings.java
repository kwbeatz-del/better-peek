package com.betterpeek.client;

import com.betterpeek.BetterPeekMod;
import com.betterpeek.client.detect.WorldContainerDetector;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/** Registers the preview toggle and the in-game HUD editor keybind. */
public final class BetterPeekKeybindings {
    private static final KeyBinding.Category CATEGORY =
        KeyBinding.Category.create(Identifier.of(BetterPeekMod.MOD_ID, "main"));

    private KeyBinding togglePreviewKey;
    private KeyBinding openEditorKey;

    public void register() {
        togglePreviewKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + BetterPeekMod.MOD_ID + ".toggle_preview",
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY
        ));
        openEditorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + BetterPeekMod.MOD_ID + ".open_hud_editor",
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY
        ));
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        while (togglePreviewKey.wasPressed()) PreviewState.get().toggle();
        while (openEditorKey.wasPressed()) {
            var worldPreview = new WorldContainerDetector().detect(client);
            client.setScreen(new HudEditorScreen(worldPreview, null));
        }
    }
}
