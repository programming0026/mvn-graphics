package com.juniverse.graphics.raster;

import com.juniverse.graphics.dto.Point;

import java.util.Arrays;
import java.util.List;

/**
 * Polygon fill by scanline with the even-odd rule.
 */
public final class PolygonRasterizer {

    private PolygonRasterizer() {
    }

    /**
     * Generates the horizontal spans of the polygon's interior. The polygon is
     * closed automatically between the last and the first vertex.
     */
    public static void fill(List<Point> vertices, SpanSink sink) {
        int count = vertices.size();
        if (count < 3) {
            vertices.forEach(p -> sink.accept(p.y(), p.x(), p.x()));
            return;
        }
        int minY = vertices.stream().mapToInt(Point::y).min().orElseThrow();
        int maxY = vertices.stream().mapToInt(Point::y).max().orElseThrow();
        double[] crossings = new double[count];

        for (int y = minY; y <= maxY; y++) {
            double scanY = y + 0.5;
            int found = 0;
            for (int i = 0; i < count; i++) {
                Point a = vertices.get(i);
                Point b = vertices.get((i + 1) % count);
                if (a.y() == b.y()) {
                    continue;
                }
                double low = Math.min(a.y(), b.y());
                double high = Math.max(a.y(), b.y());
                if (scanY >= low && scanY < high) {
                    double t = (scanY - a.y()) / (b.y() - a.y());
                    crossings[found++] = a.x() + t * (b.x() - a.x());
                }
            }
            Arrays.sort(crossings, 0, found);
            for (int i = 0; i + 1 < found; i += 2) {
                int start = (int) Math.round(crossings[i]);
                int end = (int) Math.round(crossings[i + 1]);
                if (end >= start) {
                    sink.accept(y, start, end);
                }
            }
        }
    }
}
