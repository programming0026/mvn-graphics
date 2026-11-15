package com.juniverse.graphics.raster;

/**
 * Rasterizado de segmentos de recta mediante el algoritmo de Bresenham.
 */
public final class LineRasterizer {

    private LineRasterizer() {
    }

    /**
     * Genera, en orden, todos los píxeles del segmento (ambos extremos incluidos).
     */
    public static void trace(int x0, int y0, int x1, int y1, PointSink sink) {
        int dx = Math.abs(x1 - x0);
        int dy = -Math.abs(y1 - y0);
        int stepX = x0 < x1 ? 1 : -1;
        int stepY = y0 < y1 ? 1 : -1;
        int error = dx + dy;
        int x = x0;
        int y = y0;
        while (true) {
            sink.accept(x, y);
            if (x == x1 && y == y1) {
                return;
            }
            int doubled = 2 * error;
            if (doubled >= dy) {
                error += dy;
                x += stepX;
            }
            if (doubled <= dx) {
                error += dx;
                y += stepY;
            }
        }
    }

    /** Indica si el segmento es mayormente horizontal (|dx| &gt;= |dy|). */
    public static boolean isMostlyHorizontal(int x0, int y0, int x1, int y1) {
        return Math.abs(x1 - x0) >= Math.abs(y1 - y0);
    }
}
