package com.betterpeek.client.cache;

import com.betterpeek.BetterPeekMod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Captures container contents when a handled screen opens and remembers them
 * at the block position. Double-chest snapshots are stored for both halves.
 */
public final class ContainerCachingHook {

    public void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof HandledScreen<?> handled)) {
                return;
            }
            BlockPos pos = lookedAtBlockPos(client);
            if (pos == null) {
                return;
            }
            ScreenEvents.afterTick(handled).register(s -> {
                if (!entriesAreEmpty(handled.getScreenHandler())) {
                    capture(client, handled, pos);
                }
            });
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ContainerCache.get().clear());
    }

    private static BlockPos lookedAtBlockPos(MinecraftClient client) {
        HitResult hit = client.crosshairTarget;
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            return blockHit.getBlockPos();
        }
        return null;
    }

    private static boolean entriesAreEmpty(ScreenHandler handler) {
        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof PlayerInventory) {
                break;
            }
            if (!slot.getStack().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void capture(MinecraftClient client, HandledScreen<?> screen, BlockPos pos) {
        ScreenHandler handler = screen.getScreenHandler();
        List<ItemStack> containerStacks = new ArrayList<>();
        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof PlayerInventory) {
                break;
            }
            containerStacks.add(slot.getStack());
        }
        if (containerStacks.isEmpty()) {
            return;
        }

        int slotCount = containerStacks.size();
        int columns = slotCount <= 9 ? slotCount : 9;
        int rows = (slotCount + columns - 1) / columns;
        Text title = screen.getTitle();

        ContainerCache cache = ContainerCache.get();
        cache.put(pos, title, columns, rows, containerStacks);

        BlockPos otherHalf = getOtherChestHalf(client, pos);
        if (otherHalf != null) {
            cache.put(otherHalf, title, columns, rows, containerStacks);
            BetterPeekMod.LOGGER.debug("[BetterPeek] cached double chest ({} slots) at {} and {}", slotCount, pos, otherHalf);
        } else {
            BetterPeekMod.LOGGER.debug("[BetterPeek] cached {} slots at {}", slotCount, pos);
        }
    }

    private static BlockPos getOtherChestHalf(MinecraftClient client, BlockPos pos) {
        if (client.world == null) {
            return null;
        }

        BlockState state = client.world.getBlockState(pos);
        if (!(state.getBlock() instanceof ChestBlock)) {
            return null;
        }

        ChestType chestType = state.get(ChestBlock.CHEST_TYPE);
        if (chestType == ChestType.SINGLE) {
            return null;
        }

        Direction facing = state.get(ChestBlock.FACING);
        Direction offset = chestType == ChestType.LEFT
            ? facing.rotateYCounterclockwise()
            : facing.rotateYClockwise();
        BlockPos otherPos = pos.offset(offset);

        BlockState otherState = client.world.getBlockState(otherPos);
        if (!(otherState.getBlock() instanceof ChestBlock)
            || otherState.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE) {
            return null;
        }
        return otherPos;
    }
}
