package com.betterpeek.client.jade;

import com.betterpeek.client.cache.ContainerCache;
import com.betterpeek.client.render.PreviewSnapshot;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

/** Adds remembered item icons beneath Jade's normal block information. */
public final class ContainerContentsProvider implements IBlockComponentProvider {
    public static final ContainerContentsProvider INSTANCE = new ContainerContentsProvider();
    private static final Identifier UID = Identifier.of("betterpeek", "container_contents");

    private ContainerContentsProvider() {}

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        Block block = state.getBlock();
        if (!(block instanceof ChestBlock
            || block instanceof BarrelBlock
            || block instanceof HopperBlock
            || block instanceof ShulkerBoxBlock
            || block instanceof DispenserBlock
            || block instanceof DropperBlock
            || block instanceof AbstractFurnaceBlock)) {
            return;
        }

        PreviewSnapshot snapshot = ContainerCache.get().get(accessor.getPosition());
        if (snapshot == null || snapshot.isEmpty()) {
            return;
        }

        int column = 0;
        boolean rowStarted = false;
        for (ItemStack stack : snapshot.stacks()) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            Element item = JadeUI.item(stack.copy());
            if (!rowStarted || column >= 9) {
                tooltip.add(item);
                rowStarted = true;
                column = 1;
            } else {
                tooltip.append(item);
                column++;
            }
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
