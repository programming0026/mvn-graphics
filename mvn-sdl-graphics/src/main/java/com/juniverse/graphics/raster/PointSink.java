package com.juniverse.graphics.raster;

/**
 * Receptor de puntos generados por un algoritmo de rasterizado.
 */
@FunctionalInterface
public interface PointSink {

    void accept(int x, int y);
}
