package com.juniverse.graphics.image;

/**
 * Imagen inmutable en memoria, en formato ARGB.
 * <p>
 * Sustituye al bloque de memoria opaco ({@code void* bitmap}) que usaban
 * {@code getimage} y {@code putimage} en WinBGI.
 */
public final class BgiImage {

    /** Bytes reservados para el encabezado (ancho y alto) en {@link #byteSize}. */
    private static final int HEADER_BYTES = 2 * Integer.BYTES;

    private final int width;
    private final int height;
    private final int[] pixels;

    public BgiImage(int width, int height, int[] pixels) {
        if (width <= 0 || height <= 0 || pixels.length != width * height) {
            throw new IllegalArgumentException("Inconsistent image dimensions");
        }
        this.width = width;
        this.height = height;
        this.pixels = pixels.clone();
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /** Píxel ARGB en la posición indicada. */
    public int pixel(int x, int y) {
        return pixels[y * width + x];
    }

    /** Copia de todos los píxeles ARGB, fila por fila. */
    public int[] pixels() {
        return pixels.clone();
    }

    /** Devuelve una copia redimensionada (vecino más cercano). */
    public BgiImage scaledTo(int newWidth, int newHeight) {
        if (newWidth == width && newHeight == height) {
            return this;
        }
        int[] scaled = new int[newWidth * newHeight];
        for (int y = 0; y < newHeight; y++) {
            int sourceY = (int) ((long) y * height / newHeight);
            for (int x = 0; x < newWidth; x++) {
                int sourceX = (int) ((long) x * width / newWidth);
                scaled[y * newWidth + x] = pixels[sourceY * width + sourceX];
            }
        }
        return new BgiImage(newWidth, newHeight, scaled);
    }

    /** Memoria aproximada que ocupa una imagen del tamaño indicado (equivale a {@code imagesize}). */
    public static int byteSize(int width, int height) {
        return HEADER_BYTES + Math.abs(width) * Math.abs(height) * Integer.BYTES;
    }
}
