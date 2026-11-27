package com.betterpeek.client;

import com.betterpeek.BetterPeekMod;
import com.betterpeek.client.cache.ContainerCachingHook;
import com.betterpeek.client.render.ContainerPreviewRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/** Client entry point for BetterPeek's world-container preview. */
public final class BetterPeekClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BetterPeekMod.LOGGER.info("[BetterPeek] client init");

        new BetterPeekKeybindings().register();
        new ContainerCachingHook().register();

        ContainerPreviewRenderer renderer = new ContainerPreviewRenderer();
        HudRenderCallback.EVENT.register(renderer::renderHud);
    }
}
