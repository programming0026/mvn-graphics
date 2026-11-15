package com.juniverse.graphics.raster;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Conjunto ordenado de píxeles sin duplicados.
 * <p>
 * Permite construir un contorno completo (por ejemplo, un rectángulo o una
 * polilínea) y dibujar cada píxel una sola vez. Esto es indispensable para el
 * modo XOR, donde dibujar dos veces el mismo píxel lo borraría.
 */
public final class PixelSet implements PointSink {

    private final Set<Long> pixels = new LinkedHashSet<>();

    @Override
    public void accept(int x, int y) {
        pixels.add(pack(x, y));
    }

    /** Número de píxeles distintos. */
    public int size() {
        return pixels.size();
    }

    /** Indica si el píxel ya forma parte del conjunto. */
    public boolean contains(int x, int y) {
        return pixels.contains(pack(x, y));
    }

    /** Recorre los píxeles en el orden en que se agregaron. */
    public void forEach(PointSink sink) {
        for (long packed : pixels) {
            sink.accept((int) (packed >> 32), (int) packed);
        }
    }

    private static long pack(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }
}
