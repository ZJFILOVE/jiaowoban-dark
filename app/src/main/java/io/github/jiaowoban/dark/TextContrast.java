package io.github.jiaowoban.dark;

/** Palette policy for neutral labels missed by OEM Force Dark, without altering brand colors. */
final class TextContrast {
    private TextContrast() {}

    static int forDarkBackground(int color, boolean enabled) {
        int alpha = color >>> 24;
        int red = (color >>> 16) & 255;
        int green = (color >>> 8) & 255;
        int blue = color & 255;
        int max = Math.max(red, Math.max(green, blue));
        int min = Math.min(red, Math.min(green, blue));
        if (alpha == 0 || max - min > 18 || max >= 160) return color;
        return (Math.max(alpha, enabled ? 179 : alpha) << 24) | 0x00d4d7dc;
    }
}
