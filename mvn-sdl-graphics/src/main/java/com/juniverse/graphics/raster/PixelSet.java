package com.juniverse.graphics.raster;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Ordered set of pixels without duplicates.
 * <p>
 * It allows building a complete outline (for example, a rectangle or a
 * polyline) and drawing each pixel only once. This is essential for
 * XOR mode, where drawing the same pixel twice would erase it.
 */
public final class PixelSet implements PointSink {

    private final Set<Long> pixels = new LinkedHashSet<>();

    @Override
    public void accept(int x, int y) {
        pixels.add(pack(x, y));
    }

    /** Number of distinct pixels. */
    public int size() {
        return pixels.size();
    }

    /** Tells whether the pixel is already part of the set. */
    public boolean contains(int x, int y) {
        return pixels.contains(pack(x, y));
    }

    /** Iterates over the pixels in the order they were added. */
    public void forEach(PointSink sink) {
        for (long packed : pixels) {
            sink.accept((int) (packed >> 32), (int) packed);
        }
    }

    private static long pack(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }
}
