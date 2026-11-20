package com.juniverse.graphics.text;

/**
 * Monochrome bitmap of an already rasterized text.
 * <p>
 * It is immutable: transformations return a new mask.
 */
public final class TextMask {

    private final int width;
    private final int height;
    private final boolean[] bits;

    public TextMask(int width, int height, boolean[] bits) {
        if (width < 0 || height < 0 || bits.length != width * height) {
            throw new IllegalArgumentException("Inconsistent mask dimensions");
        }
        this.width = width;
        this.height = height;
        this.bits = bits.clone();
    }

    /** Empty mask with the given height (for example, for an empty text). */
    public static TextMask empty(int height) {
        return new TextMask(0, height, new boolean[0]);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /** Tells whether pixel (x, y) belongs to some glyph. */
    public boolean isSet(int x, int y) {
        return bits[y * width + x];
    }

    /** Returns the mask rotated 90° counterclockwise (vertical text). */
    public TextMask rotateCounterClockwise() {
        boolean[] rotated = new boolean[bits.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int newX = y;
                int newY = width - 1 - x;
                rotated[newY * height + newX] = bits[y * width + x];
            }
        }
        return new TextMask(height, width, rotated);
    }
}
