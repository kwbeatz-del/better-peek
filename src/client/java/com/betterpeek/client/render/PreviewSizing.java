package com.betterpeek.client.render;

/** Fits container grids into a predictable HUD footprint while preserving user scale. */
public final class PreviewSizing {
    private static final float MAX_WIDTH = 144.0f;
    private static final float MAX_HEIGHT = 104.0f;
    private PreviewSizing() {}

    public static float effectiveScale(PreviewSnapshot snapshot, float userScale) {
        float contentWidth = snapshot.columns() * 16.0f + 8.0f;
        float contentHeight = snapshot.rows() * 16.0f + 20.0f;
        float fit = Math.min(1.0f, Math.min(MAX_WIDTH / contentWidth, MAX_HEIGHT / contentHeight));
        return userScale * fit;
    }
    public static int panelWidth(PreviewSnapshot snapshot, float scale) {
        return Math.round((snapshot.columns() * 16 + 8) * scale);
    }
    public static int panelHeight(PreviewSnapshot snapshot, float scale) {
        return Math.round((snapshot.rows() * 16 + 20) * scale);
    }
}