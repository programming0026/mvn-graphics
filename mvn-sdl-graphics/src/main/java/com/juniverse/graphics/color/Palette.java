package com.juniverse.graphics.color;

import java.util.Arrays;

/**
 * Paleta de 16 entradas que traduce códigos de color BGI a valores ARGB.
 * <p>
 * Su única responsabilidad es la conversión entre códigos de color
 * ({@link ColorCode}) y píxeles ARGB, además de permitir redefinir las
 * entradas (equivalente a {@code setpalette}, {@code setrgbpalette}, etc.).
 */
public final class Palette {

    /** Número de entradas de la paleta (equivale a {@code getpalettesize()}). */
    public static final int SIZE = BgiColor.MAX_COLOR + 1;

    private final int[] entries = new int[SIZE];

    /** Crea una paleta inicializada con los colores por defecto. */
    public Palette() {
        reset();
    }

    /** Restaura los colores VGA por defecto. */
    public void reset() {
        for (BgiColor color : BgiColor.values()) {
            entries[color.code()] = color.defaultRgb();
        }
    }

    /**
     * Convierte un código de color (BGI o RGB) a un píxel ARGB opaco.
     */
    public int toArgb(int colorCode) {
        int rgb = ColorCode.isBgiColor(colorCode) ? entries[colorCode] : colorCode & ColorCode.RGB_MASK;
        return ColorCode.OPAQUE | rgb;
    }

    /**
     * Convierte un píxel ARGB al código de color que lo representa: si coincide
     * con alguna entrada de la paleta se devuelve su índice; en caso contrario
     * se devuelve el código RGB ({@code 0x03RRGGBB}).
     */
    public int toColorCode(int argb) {
        int rgb = argb & ColorCode.RGB_MASK;
        for (int index = 0; index < SIZE; index++) {
            if (entries[index] == rgb) {
                return index;
            }
        }
        return ColorCode.RGB_FLAG | rgb;
    }

    /** Devuelve el valor 0xRRGGBB de un código de color. */
    public int toRgb(int colorCode) {
        return toArgb(colorCode) & ColorCode.RGB_MASK;
    }

    /**
     * Asigna a la entrada {@code index} el color indicado por {@code colorCode}
     * (equivalente a {@code setpalette}).
     */
    public void setEntry(int index, int colorCode) {
        checkIndex(index);
        int rgb = ColorCode.isBgiColor(colorCode)
                ? BgiColor.fromCode(colorCode).defaultRgb()
                : colorCode & ColorCode.RGB_MASK;
        entries[index] = rgb;
    }

    /** Asigna un valor RGB a una entrada (equivalente a {@code setrgbpalette}). */
    public void setRgb(int index, int red, int green, int blue) {
        checkIndex(index);
        entries[index] = ColorCode.rgb(red, green, blue) & ColorCode.RGB_MASK;
    }

    /** Copia de las entradas actuales en formato 0xRRGGBB (equivale a {@code getpalette}). */
    public int[] entries() {
        return entries.clone();
    }

    /** Reemplaza todas las entradas (equivale a {@code setallpalette}). */
    public void setAll(int[] rgbEntries) {
        if (rgbEntries.length != SIZE) {
            throw new IllegalArgumentException("Palette must have " + SIZE + " entries");
        }
        for (int i = 0; i < SIZE; i++) {
            entries[i] = rgbEntries[i] & ColorCode.RGB_MASK;
        }
    }

    /** Entradas por defecto (equivale a {@code getdefaultpalette}). */
    public static int[] defaultEntries() {
        return Arrays.stream(BgiColor.values()).mapToInt(BgiColor::defaultRgb).toArray();
    }

    private static void checkIndex(int index) {
        if (index < 0 || index >= SIZE) {
            throw new IllegalArgumentException("Palette index out of range: " + index);
        }
    }
}
