package com.juniverse.graphics.color;

/**
 * Utilidades para el formato de "código de color" heredado de WinBGI.
 * <p>
 * Un código de color es un {@code int} que puede representar:
 * <ul>
 *     <li>Un índice de la paleta BGI (0..15).</li>
 *     <li>Un color RGB arbitrario codificado como {@code 0x03RRGGBB}
 *     (equivalente a la función {@code COLOR(r, g, b)} de WinBGI).</li>
 * </ul>
 * Internamente los píxeles se almacenan en formato ARGB ({@code 0xAARRGGBB});
 * la conversión entre ambos formatos la realiza {@link Palette}.
 */
public final class ColorCode {

    /** Marca que identifica un código RGB (igual que en WinBGI). */
    public static final int RGB_FLAG = 0x03000000;

    /** Máscara de los 24 bits de color. */
    public static final int RGB_MASK = 0x00FFFFFF;

    /** Canal alfa opaco usado por el framebuffer. */
    public static final int OPAQUE = 0xFF000000;

    private ColorCode() {
    }

    /** Crea un código de color RGB (equivalente a {@code COLOR(r,g,b)}). */
    public static int rgb(int red, int green, int blue) {
        return RGB_FLAG | (clamp(red) << 16) | (clamp(green) << 8) | clamp(blue);
    }

    /** Indica si el código corresponde a un índice de la paleta BGI. */
    public static boolean isBgiColor(int code) {
        return code >= 0 && code <= BgiColor.MAX_COLOR;
    }

    /** Indica si el código corresponde a un color RGB codificado. */
    public static boolean isRgbColor(int code) {
        return (code & RGB_FLAG) != 0;
    }

    /** Componente roja de un valor 0xRRGGBB o ARGB. */
    public static int red(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    /** Componente verde de un valor 0xRRGGBB o ARGB. */
    public static int green(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    /** Componente azul de un valor 0xRRGGBB o ARGB. */
    public static int blue(int rgb) {
        return rgb & 0xFF;
    }

    private static int clamp(int component) {
        return Math.max(0, Math.min(255, component));
    }
}
