package com.betterpeek.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persistent client-side layout for BetterPeek's two independent previews. */
public final class HudLayoutConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("betterpeek-hud.json");
    private static HudLayoutConfig INSTANCE = load();

    public int worldX = -1;
    public int worldY = -1;
    public float worldScale = 1.0f;
    public int inventoryX = -1;
    public int inventoryY = -1;
    public float inventoryScale = 1.0f;

    private static HudLayoutConfig load() {
        try {
            if (Files.exists(FILE)) {
                HudLayoutConfig loaded = GSON.fromJson(Files.readString(FILE), HudLayoutConfig.class);
                if (loaded != null) return loaded;
            }
        } catch (Exception ignored) {
            // A malformed config should not prevent the client from starting.
        }
        return new HudLayoutConfig();
    }

    public static HudLayoutConfig get() { return INSTANCE; }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this));
        } catch (IOException exception) {
            System.err.println("[BetterPeek] Could not save HUD layout: " + exception.getMessage());
        }
    }

    public void reset() {
        worldX = -1;
        worldY = -1;
        worldScale = 1.0f;
        inventoryX = -1;
        inventoryY = -1;
        inventoryScale = 1.0f;
        save();
    }
}
