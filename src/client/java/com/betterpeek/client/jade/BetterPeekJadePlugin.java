package com.betterpeek.client.jade;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/** Optional Jade integration for remembered container contents. */
@WailaPlugin
public final class BetterPeekJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        // All inventory snapshots are captured client-side when a container GUI opens.
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(ContainerContentsProvider.INSTANCE, Block.class);
    }
}
