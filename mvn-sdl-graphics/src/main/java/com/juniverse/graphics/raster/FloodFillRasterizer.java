package com.juniverse.graphics.raster;

import com.juniverse.graphics.dto.Rect;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;

/**
 * Flood fill bounded by a border color.
 * <p>
 * It first computes the whole region (without modifying the canvas) and then
 * delivers it as horizontal spans. Separating the computation from the painting allows
 * filling with patterns without the fill itself altering the search.
 */
public final class FloodFillRasterizer {

    private FloodFillRasterizer() {
    }

    /**
     * Computes the connected region (4-neighborhood) that contains the seed and
     * whose pixels differ from the border color.
     *
     * @param canvas     canvas to analyze
     * @param seedX      seed column (viewport-relative)
     * @param seedY      seed row (viewport-relative)
     * @param borderArgb ARGB color that delimits the region
     * @param sink       receiver of the region's spans
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

    /** Search state: canvas limits and already visited pixels. */
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
