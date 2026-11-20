package com.juniverse.graphics.color;

import java.util.Arrays;

/**
 * 16-entry palette that translates BGI color codes to ARGB values.
 * <p>
 * Its only responsibility is the conversion between color codes
 * ({@link ColorCode}) and ARGB pixels, plus allowing the entries to be
 * redefined (equivalent to {@code setpalette}, {@code setrgbpalette}, etc.).
 */
public final class Palette {

    /** Number of palette entries (equivalent to {@code getpalettesize()}). */
    public static final int SIZE = BgiColor.MAX_COLOR + 1;

    private final int[] entries = new int[SIZE];

    /** Creates a palette initialized with the default colors. */
    public Palette() {
        reset();
    }

    /** Restores the default VGA colors. */
    public void reset() {
        for (BgiColor color : BgiColor.values()) {
            entries[color.code()] = color.defaultRgb();
        }
    }

    /**
     * Converts a color code (BGI or RGB) to an opaque ARGB pixel.
     */
    public int toArgb(int colorCode) {
        int rgb = ColorCode.isBgiColor(colorCode) ? entries[colorCode] : colorCode & ColorCode.RGB_MASK;
        return ColorCode.OPAQUE | rgb;
    }

    /**
     * Converts an ARGB pixel to the color code that represents it: if it matches
     * a palette entry its index is returned; otherwise
     * the RGB code ({@code 0x03RRGGBB}) is returned.
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

    /** Returns the 0xRRGGBB value of a color code. */
    public int toRgb(int colorCode) {
        return toArgb(colorCode) & ColorCode.RGB_MASK;
    }

    /**
     * Assigns to entry {@code index} the color given by {@code colorCode}
     * (equivalent to {@code setpalette}).
     */
    public void setEntry(int index, int colorCode) {
        checkIndex(index);
        int rgb = ColorCode.isBgiColor(colorCode)
                ? BgiColor.fromCode(colorCode).defaultRgb()
                : colorCode & ColorCode.RGB_MASK;
        entries[index] = rgb;
    }

    /** Assigns an RGB value to an entry (equivalent to {@code setrgbpalette}). */
    public void setRgb(int index, int red, int green, int blue) {
        checkIndex(index);
        entries[index] = ColorCode.rgb(red, green, blue) & ColorCode.RGB_MASK;
    }

    /** Copy of the current entries in 0xRRGGBB format (equivalent to {@code getpalette}). */
    public int[] entries() {
        return entries.clone();
    }

    /** Replaces all entries (equivalent to {@code setallpalette}). */
    public void setAll(int[] rgbEntries) {
        if (rgbEntries.length != SIZE) {
            throw new IllegalArgumentException("Palette must have " + SIZE + " entries");
        }
        for (int i = 0; i < SIZE; i++) {
            entries[i] = rgbEntries[i] & ColorCode.RGB_MASK;
        }
    }

    /** Default entries (equivalent to {@code getdefaultpalette}). */
    public static int[] defaultEntries() {
        return Arrays.stream(BgiColor.values()).mapToInt(BgiColor::defaultRgb).toArray();
    }

    private static void checkIndex(int index) {
        if (index < 0 || index >= SIZE) {
            throw new IllegalArgumentException("Palette index out of range: " + index);
        }
    }
}
