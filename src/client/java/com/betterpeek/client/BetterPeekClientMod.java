package com.betterpeek.client;

import com.betterpeek.BetterPeekMod;
import com.betterpeek.client.cache.ContainerCachingHook;
import net.fabricmc.api.ClientModInitializer;

/** Client entry point for BetterPeek's container cache. */
public final class BetterPeekClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BetterPeekMod.LOGGER.info("[BetterPeek] client init");
        new ContainerCachingHook().register();
        // Jade now owns presentation; the standalone HUD renderer is intentionally disabled.
    }
}
