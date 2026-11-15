package com.juniverse.graphics.raster;

import com.juniverse.graphics.dto.Point;
import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.style.RasterOperation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RasterizerTest {

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    @Test
    public void lineIncludesBothEndpointsAndIsContinuous() {
        List<Point> points = new ArrayList<>();
        LineRasterizer.trace(0, 0, 5, 2, (x, y) -> points.add(new Point(x, y)));

        assertEquals(new Point(0, 0), points.get(0));
        assertEquals(new Point(5, 2), points.get(points.size() - 1));
        assertEquals(6, points.size());
        for (int i = 1; i < points.size(); i++) {
            assertTrue(Math.abs(points.get(i).x() - points.get(i - 1).x()) <= 1);
            assertTrue(Math.abs(points.get(i).y() - points.get(i - 1).y()) <= 1);
        }
    }

    @Test
    public void polygonFillCoversInteriorOfSquare() {
        int[] count = {0};
        PolygonRasterizer.fill(List.of(new Point(0, 0), new Point(10, 0), new Point(10, 10), new Point(0, 10)),
                (y, start, end) -> count[0] += end - start + 1);
        assertEquals(11 * 10, count[0]);
    }

    @Test
    public void ellipseFillProducesSymmetricSpans() {
        List<int[]> spans = new ArrayList<>();
        EllipseRasterizer.fillEllipse(50, 50, 10, 5, (y, start, end) -> spans.add(new int[]{y, start, end}));
        assertEquals(11, spans.size());
        for (int[] span : spans) {
            assertEquals(50 - span[1], span[2] - 50);
        }
    }

    @Test
    public void floodFillStopsAtBorder() {
        FrameBuffer page = new FrameBuffer(20, 20);
        page.fill(BLACK);
        Canvas canvas = new ViewportCanvas(() -> page, () -> Viewport.fullScreen(20, 20));
        for (int i = 5; i <= 15; i++) {
            canvas.plot(i, 5, WHITE, RasterOperation.COPY);
            canvas.plot(i, 15, WHITE, RasterOperation.COPY);
            canvas.plot(5, i, WHITE, RasterOperation.COPY);
            canvas.plot(15, i, WHITE, RasterOperation.COPY);
        }
        int[] filled = {0};
        FloodFillRasterizer.fill(canvas, 10, 10, WHITE, (y, start, end) -> filled[0] += end - start + 1);
        assertEquals(9 * 9, filled[0]);
    }

    @Test
    public void viewportTranslatesAndClips() {
        FrameBuffer page = new FrameBuffer(100, 100);
        page.fill(BLACK);
        Canvas canvas = new ViewportCanvas(() -> page, () -> new Viewport(10, 10, 19, 19, true));

        canvas.plot(0, 0, WHITE, RasterOperation.COPY);
        canvas.plot(50, 50, WHITE, RasterOperation.COPY);

        assertEquals(WHITE, page.get(10, 10));
        assertEquals(BLACK, page.get(60, 60));
        assertFalse(canvas.isDrawable(10, 0));
        assertEquals(Canvas.OUTSIDE, canvas.read(-1, 0));
    }

    @Test
    public void pixelSetIgnoresDuplicates() {
        PixelSet set = new PixelSet();
        set.accept(1, 1);
        set.accept(1, 1);
        set.accept(-3, 2);
        assertEquals(2, set.size());
        assertTrue(set.contains(-3, 2));
    }
}
