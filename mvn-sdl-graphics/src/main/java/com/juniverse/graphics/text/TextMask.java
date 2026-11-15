package com.juniverse.graphics.text;

/**
 * Mapa de bits monocromático de un texto ya rasterizado.
 * <p>
 * Es inmutable: las transformaciones devuelven una nueva máscara.
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

    /** Máscara vacía con el alto indicado (por ejemplo, para un texto vacío). */
    public static TextMask empty(int height) {
        return new TextMask(0, height, new boolean[0]);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /** Indica si el píxel (x, y) pertenece a algún glifo. */
    public boolean isSet(int x, int y) {
        return bits[y * width + x];
    }

    /** Devuelve la máscara girada 90° en sentido antihorario (texto vertical). */
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
