package com.juniverse.graphics.raster;

import com.juniverse.graphics.dto.Rect;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;

/**
 * Relleno por inundación (flood fill) acotado por un color de borde.
 * <p>
 * Primero calcula toda la región (sin modificar el lienzo) y después la
 * entrega como tramos horizontales. Separar el cálculo del pintado permite
 * rellenar con patrones sin que el propio relleno altere la búsqueda.
 */
public final class FloodFillRasterizer {

    private FloodFillRasterizer() {
    }

    /**
     * Calcula la región conectada (4-vecindad) que contiene a la semilla y
     * cuyos píxeles son distintos del color de borde.
     *
     * @param canvas     lienzo a analizar
     * @param seedX      columna semilla (relativa al viewport)
     * @param seedY      fila semilla (relativa al viewport)
     * @param borderArgb color ARGB que delimita la región
     * @param sink       receptor de los tramos de la región
     */
    public static void fill(Canvas canvas, int seedX, int seedY, int borderArgb, SpanSink sink) {
        Region region = new Region(canvas, borderArgb);
        if (!region.isCandidate(seedX, seedY)) {
            return;
        }
        List<int[]> spans = new ArrayList<>();
        Deque<int[]> pending = new ArrayDeque<>();
        pending.push(new int[]{seedX, seedY});

        while (!pending.isEmpty()) {
            int[] seed = pending.pop();
            int y = seed[1];
            if (!region.isCandidate(seed[0], y)) {
                continue;
            }
            int left = seed[0];
            while (region.isCandidate(left - 1, y)) {
                left--;
            }
            int right = seed[0];
            while (region.isCandidate(right + 1, y)) {
                right++;
            }
            region.markVisited(y, left, right);
            spans.add(new int[]{y, left, right});
            queueNeighbors(region, left, right, y - 1, pending);
            queueNeighbors(region, left, right, y + 1, pending);
        }
        spans.forEach(span -> sink.accept(span[0], span[1], span[2]));
    }

    private static void queueNeighbors(Region region, int left, int right, int y, Deque<int[]> pending) {
        boolean inRun = false;
        for (int x = left; x <= right; x++) {
            boolean candidate = region.isCandidate(x, y);
            if (candidate && !inRun) {
                pending.push(new int[]{x, y});
            }
            inRun = candidate;
        }
    }

    /** Estado de la búsqueda: límites del lienzo y píxeles ya visitados. */
    private static final class Region {
        private final Canvas canvas;
        private final int borderArgb;
        private final Rect bounds;
        private final int stride;
        private final BitSet visited = new BitSet();

        private Region(Canvas canvas, int borderArgb) {
            this.canvas = canvas;
            this.borderArgb = borderArgb;
            this.bounds = canvas.drawableBounds();
            this.stride = bounds.width();
        }

        private boolean isCandidate(int x, int y) {
            return canvas.isDrawable(x, y)
                    && !visited.get(index(x, y))
                    && canvas.read(x, y) != borderArgb;
        }

        private void markVisited(int y, int left, int right) {
            visited.set(index(left, y), index(right, y) + 1);
        }

        private int index(int x, int y) {
            return (y - bounds.top()) * stride + (x - bounds.left());
        }
    }
}
