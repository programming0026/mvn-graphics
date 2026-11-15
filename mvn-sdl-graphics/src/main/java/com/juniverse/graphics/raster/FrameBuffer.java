package com.juniverse.graphics.raster;

import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * Página de dibujo en memoria: una matriz de píxeles ARGB.
 * <p>
 * Es el equivalente a cada uno de los "device contexts" de memoria que
 * WinBGI usaba para la doble/múltiple página. No sabe nada de ventanas ni de
 * SDL: solo almacena píxeles.
 */
public final class FrameBuffer {

    private final int width;
    private final int height;
    private final int[] pixels;

    public FrameBuffer(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid dimensions: " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /** Indica si la coordenada absoluta está dentro de la página. */
    public boolean contains(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    /** Lee un píxel ARGB. La coordenada debe estar dentro de la página. */
    public int get(int x, int y) {
        return pixels[y * width + x];
    }

    /** Escribe un píxel ARGB. La coordenada debe estar dentro de la página. */
    public void set(int x, int y, int argb) {
        pixels[y * width + x] = argb;
    }

    /** Rellena toda la página con un color. */
    public void fill(int argb) {
        Arrays.fill(pixels, argb);
    }

    /** Copia el contenido de otra página de las mismas dimensiones. */
    public void copyFrom(FrameBuffer other) {
        if (other.width != width || other.height != height) {
            throw new IllegalArgumentException("Pages have different dimensions");
        }
        System.arraycopy(other.pixels, 0, pixels, 0, pixels.length);
    }

    /**
     * Copia la zona común con otra página de cualquier tamaño, anclada en la
     * esquina superior izquierda (se usa al cambiar el tamaño de la página).
     */
    public void copyOverlapFrom(FrameBuffer other) {
        int copyWidth = Math.min(width, other.width);
        int copyHeight = Math.min(height, other.height);
        for (int y = 0; y < copyHeight; y++) {
            System.arraycopy(other.pixels, y * other.width, pixels, y * width, copyWidth);
        }
    }

    /**
     * Vuelca los píxeles en un búfer (por ejemplo, la memoria de una textura).
     * El búfer destino debe tener capacidad para {@code width * height} enteros.
     */
    public void copyTo(IntBuffer target) {
        copyTo(target, width, height);
    }

    /**
     * Vuelca solo la región superior izquierda indicada (por ejemplo, la parte
     * visible de una página más grande que la ventana). El búfer destino debe
     * tener capacidad para {@code regionWidth * regionHeight} enteros.
     */
    public void copyTo(IntBuffer target, int regionWidth, int regionHeight) {
        if (regionWidth > width || regionHeight > height) {
            throw new IllegalArgumentException("Region exceeds the page size");
        }
        target.clear();
        if (regionWidth == width) {
            target.put(pixels, 0, width * regionHeight);
        } else {
            for (int y = 0; y < regionHeight; y++) {
                target.put(pixels, y * width, regionWidth);
            }
        }
        target.flip();
    }
}
