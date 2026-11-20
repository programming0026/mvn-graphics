package com.juniverse.graphics.image;

/**
 * Immutable in-memory image, in ARGB format.
 * <p>
 * Replaces the opaque memory block ({@code void* bitmap}) used by
 * {@code getimage} and {@code putimage} in WinBGI.
 */
public final class BgiImage {

    /** Bytes reserved for the header (width and height) in {@link #byteSize}. */
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

    /** ARGB pixel at the given position. */
    public int pixel(int x, int y) {
        return pixels[y * width + x];
    }

    /** Copy of all ARGB pixels, row by row. */
    public int[] pixels() {
        return pixels.clone();
    }

    /** Returns a resized copy (nearest neighbor). */
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

    /** Approximate memory taken by an image of the given size (equivalent to {@code imagesize}). */
    public static int byteSize(int width, int height) {
        return HEADER_BYTES + Math.abs(width) * Math.abs(height) * Integer.BYTES;
    }
}
