package com.juniverse.graphics.drawing;

import com.juniverse.graphics.dto.ArcCoordinates;
import com.juniverse.graphics.dto.Point;
import com.juniverse.graphics.raster.Canvas;
import com.juniverse.graphics.raster.EllipseRasterizer;

import java.util.List;

/**
 * Implementación de {@link ShapeOperations} sobre un {@link DrawingContext}.
 */
public final class ShapeDrawer implements ShapeOperations {

    private final DrawingContext context;
    private final StrokePainter stroke;

    public ShapeDrawer(DrawingContext context) {
        this.context = context;
        this.stroke = new StrokePainter(context);
    }

    @Override
    public void putPixel(int x, int y, int colorCode) {
        DrawingState state = context.state();
        context.canvas().plot(x, y, state.palette().toArgb(colorCode), state.writeMode());
        context.changed();
    }

    @Override
    public int getPixel(int x, int y) {
        int argb = context.canvas().read(x, y);
        return argb == Canvas.OUTSIDE ? 0 : context.state().palette().toColorCode(argb);
    }

    @Override
    public void rectangle(int left, int top, int right, int bottom) {
        stroke.strokePolyline(List.of(
                new Point(left, top), new Point(right, top),
                new Point(right, bottom), new Point(left, bottom),
                new Point(left, top)));
        context.changed();
    }

    @Override
    public void circle(int x, int y, int radius) {
        stroke.strokeArc(x, y, 0, 360, radius, radius);
        context.changed();
    }

    @Override
    public void arc(int x, int y, int startAngle, int endAngle, int radius) {
        ellipse(x, y, startAngle, endAngle, radius, radius);
    }

    @Override
    public void ellipse(int x, int y, int startAngle, int endAngle, int radiusX, int radiusY) {
        stroke.strokeArc(x, y, startAngle, endAngle, radiusX, radiusY);
        rememberArc(context.state(), x, y, startAngle, endAngle, radiusX, radiusY);
        context.changed();
    }

    @Override
    public void drawPoly(List<Point> points) {
        if (points.isEmpty()) {
            return;
        }
        stroke.strokePolyline(points);
        context.changed();
    }

    /** Guarda los extremos del arco para {@code getarccoords}. */
    public static void rememberArc(DrawingState state, int x, int y, int startAngle, int endAngle,
                            int radiusX, int radiusY) {
        Point start = EllipseRasterizer.pointAt(x, y, radiusX, radiusY, startAngle);
        Point end = EllipseRasterizer.pointAt(x, y, radiusX, radiusY, endAngle);
        state.setLastArc(new ArcCoordinates(x, y, start.x(), start.y(), end.x(), end.y()));
    }
}
