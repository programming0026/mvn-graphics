package com.juniverse.graphics.color;

/**
 * Utilities for the "color code" format inherited from WinBGI.
 * <p>
 * A color code is an {@code int} that can represent:
 * <ul>
 *     <li>An index of the BGI palette (0..15).</li>
 *     <li>An arbitrary RGB color encoded as {@code 0x03RRGGBB}
 *     (equivalent to WinBGI's {@code COLOR(r, g, b)} function).</li>
 * </ul>
 * Internally pixels are stored in ARGB format ({@code 0xAARRGGBB});
 * the conversion between both formats is done by {@link Palette}.
 */
public final class ColorCode {

    /** Marker that identifies an RGB code (same as in WinBGI). */
    public static final int RGB_FLAG = 0x03000000;

    /** Mask of the 24 color bits. */
    public static final int RGB_MASK = 0x00FFFFFF;

    /** Opaque alpha channel used by the framebuffer. */
    public static final int OPAQUE = 0xFF000000;

    private ColorCode() {
    }

    /** Creates an RGB color code (equivalent to {@code COLOR(r,g,b)}). */
    public static int rgb(int red, int green, int blue) {
        return RGB_FLAG | (clamp(red) << 16) | (clamp(green) << 8) | clamp(blue);
    }

    /** Tells whether the code is an index of the BGI palette. */
    public static boolean isBgiColor(int code) {
        return code >= 0 && code <= BgiColor.MAX_COLOR;
    }

    /** Tells whether the code is an encoded RGB color. */
    public static boolean isRgbColor(int code) {
        return (code & RGB_FLAG) != 0;
    }

    /** Red component of a 0xRRGGBB or ARGB value. */
    public static int red(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    /** Green component of a 0xRRGGBB or ARGB value. */
    public static int green(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    /** Blue component of a 0xRRGGBB or ARGB value. */
    public static int blue(int rgb) {
        return rgb & 0xFF;
    }

    private static int clamp(int component) {
        return Math.max(0, Math.min(255, component));
    }
}
