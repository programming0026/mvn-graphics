package com.juniverse.graphics.drawing;

import com.juniverse.graphics.dto.Point;
import com.juniverse.graphics.raster.EllipseRasterizer;
import com.juniverse.graphics.raster.LineRasterizer;
import com.juniverse.graphics.raster.PixelSet;
import com.juniverse.graphics.dto.LineSettings;

import java.util.List;

/**
 * Traza contornos aplicando el estilo de línea, el grosor, el color y el
 * modo de escritura actuales.
 * <p>
 * Cada contorno se acumula primero en un {@link PixelSet} para que ningún
 * píxel se dibuje dos veces (importante en modo XOR).
 */
public final class StrokePainter {

    private final DrawingContext context;

    public StrokePainter(DrawingContext context) {
        this.context = context;
    }

    /** Dibuja una polilínea con el estilo de línea actual. */
    public void strokePolyline(List<Point> points) {
        LineSettings settings = context.state().lineSettings();
        int pattern = settings.effectivePattern();
        int thickness = settings.thickness();
        PixelSet pixels = new PixelSet();
        int[] step = {0};
        for (int i = 0; i + 1 < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get(i + 1);
            boolean horizontal = LineRasterizer.isMostlyHorizontal(a.x(), a.y(), b.x(), b.y());
            LineRasterizer.trace(a.x(), a.y(), b.x(), b.y(), (x, y) -> {
                if (isPatternBitSet(pattern, step[0]++)) {
                    addThick(pixels, x, y, thickness, horizontal);
                }
            });
        }
        if (points.size() == 1) {
            Point only = points.get(0);
            addThick(pixels, only.x(), only.y(), thickness, true);
        }
        paint(pixels);
    }

    /** Dibuja un arco elíptico sólido con el grosor actual. */
    public void strokeArc(int x, int y, int startAngle, int endAngle, int radiusX, int radiusY) {
        PixelSet pixels = new PixelSet();
        addArc(pixels, x, y, startAngle, endAngle, radiusX, radiusY);
        paint(pixels);
    }

    /** Agrega a un conjunto los píxeles de un arco sólido con el grosor actual. */
    public void addArc(PixelSet pixels, int x, int y, int startAngle, int endAngle, int radiusX, int radiusY) {
        int thickness = context.state().lineSettings().thickness();
        EllipseRasterizer.traceArc(x, y, radiusX, radiusY, startAngle, endAngle,
                (px, py) -> addSquare(pixels, px, py, thickness));
    }

    /** Agrega a un conjunto los píxeles de un segmento sólido con el grosor actual. */
    public void addSolidLine(PixelSet pixels, int x0, int y0, int x1, int y1) {
        int thickness = context.state().lineSettings().thickness();
        boolean horizontal = LineRasterizer.isMostlyHorizontal(x0, y0, x1, y1);
        LineRasterizer.trace(x0, y0, x1, y1, (x, y) -> addThick(pixels, x, y, thickness, horizontal));
    }

    /** Dibuja todos los píxeles del conjunto con el color y modo actuales. */
    public void paint(PixelSet pixels) {
        DrawingState state = context.state();
        int argb = state.colorArgb();
        pixels.forEach((x, y) -> context.canvas().plot(x, y, argb, state.writeMode()));
    }

    private static boolean isPatternBitSet(int pattern, int step) {
        return (pattern & (0x8000 >>> (step & 15))) != 0;
    }

    private static void addThick(PixelSet pixels, int x, int y, int thickness, boolean horizontal) {
        int half = thickness / 2;
        for (int offset = -half; offset <= half; offset++) {
            if (horizontal) {
                pixels.accept(x, y + offset);
            } else {
                pixels.accept(x + offset, y);
            }
        }
    }

    private static void addSquare(PixelSet pixels, int x, int y, int thickness) {
        int half = thickness / 2;
        for (int dy = -half; dy <= half; dy++) {
            for (int dx = -half; dx <= half; dx++) {
                pixels.accept(x + dx, y + dy);
            }
        }
    }
}
