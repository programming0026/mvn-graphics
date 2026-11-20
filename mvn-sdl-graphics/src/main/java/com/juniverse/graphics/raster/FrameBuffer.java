package com.juniverse.graphics.raster;

import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * In-memory drawing page: an array of ARGB pixels.
 * <p>
 * It is the equivalent of each of the in-memory "device contexts" that
 * WinBGI used for double/multiple pages. It knows nothing about windows or
 * SDL: it only stores pixels.
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

    /** Tells whether the absolute coordinate is inside the page. */
    public boolean contains(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    /** Reads an ARGB pixel. The coordinate must be inside the page. */
    public int get(int x, int y) {
        return pixels[y * width + x];
    }

    /** Writes an ARGB pixel. The coordinate must be inside the page. */
    public void set(int x, int y, int argb) {
        pixels[y * width + x] = argb;
    }

    /** Fills the whole page with a color. */
    public void fill(int argb) {
        Arrays.fill(pixels, argb);
    }

    /** Copies the content of another page of the same dimensions. */
    public void copyFrom(FrameBuffer other) {
        if (other.width != width || other.height != height) {
            throw new IllegalArgumentException("Pages have different dimensions");
        }
        System.arraycopy(other.pixels, 0, pixels, 0, pixels.length);
    }

    /**
     * Copies the common area with another page of any size, anchored at the
     * top-left corner (used when the page size changes).
     */
    public void copyOverlapFrom(FrameBuffer other) {
        int copyWidth = Math.min(width, other.width);
        int copyHeight = Math.min(height, other.height);
        for (int y = 0; y < copyHeight; y++) {
            System.arraycopy(other.pixels, y * other.width, pixels, y * width, copyWidth);
        }
    }

    /**
     * Dumps the pixels into a buffer (for example, texture memory).
     * The target buffer must have capacity for {@code width * height} integers.
     */
    public void copyTo(IntBuffer target) {
        copyTo(target, width, height);
    }

    /**
     * Dumps only the given top-left region (for example, the visible
     * part of a page larger than the window). The target buffer must
     * have capacity for {@code regionWidth * regionHeight} integers.
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
