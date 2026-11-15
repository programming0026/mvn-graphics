package com.juniverse.graphics.drawing;

import com.juniverse.graphics.dto.Point;
import com.juniverse.graphics.dto.Rect;
import com.juniverse.graphics.raster.EllipseRasterizer;
import com.juniverse.graphics.raster.FloodFillRasterizer;
import com.juniverse.graphics.raster.PixelSet;
import com.juniverse.graphics.raster.PolygonRasterizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de {@link FillOperations} sobre un {@link DrawingContext}.
 */
public final class FillDrawer implements FillOperations {

    /** Ángulo de la profundidad de {@code bar3d} (30°, igual que BGI). */
    private static final double BAR3D_ANGLE = Math.toRadians(30.0);

    private final DrawingContext context;
    private final StrokePainter stroke;
    private final PatternPainter pattern;

    public FillDrawer(DrawingContext context) {
        this.context = context;
        this.stroke = new StrokePainter(context);
        this.pattern = new PatternPainter(context);
    }

    @Override
    public void bar(int left, int top, int right, int bottom) {
        fillRect(new Rect(left, top, right, bottom).normalized());
        context.changed();
    }

    @Override
    public void bar3d(int left, int top, int right, int bottom, int depth, boolean drawTop) {
        Rect box = new Rect(left, top, right, bottom).normalized();
        fillRect(box);
        int dy = (int) (depth * Math.tan(BAR3D_ANGLE));
        List<Point> outline = new ArrayList<>(List.of(
                new Point(box.left(), box.top()), new Point(box.right(), box.top()),
                new Point(box.right(), box.bottom()), new Point(box.left(), box.bottom()),
                new Point(box.left(), box.top())));
        stroke.strokePolyline(outline);
        if (depth != 0) {
            stroke.strokePolyline(List.of(
                    new Point(box.right(), box.bottom()),
                    new Point(box.right() + depth, box.bottom() - dy),
                    new Point(box.right() + depth, box.top() - dy),
                    new Point(box.right(), box.top())));
            if (drawTop) {
                stroke.strokePolyline(List.of(
                        new Point(box.right() + depth, box.top() - dy),
                        new Point(box.left() + depth, box.top() - dy),
                        new Point(box.left(), box.top())));
            }
        }
        context.changed();
    }

    @Override
    public void fillEllipse(int x, int y, int radiusX, int radiusY) {
        EllipseRasterizer.fillEllipse(x, y, radiusX, radiusY, pattern);
        stroke.strokeArc(x, y, 0, 360, radiusX, radiusY);
        context.changed();
    }

    @Override
    public void fillPoly(List<Point> points) {
        if (points.isEmpty()) {
            return;
        }
        PolygonRasterizer.fill(points, pattern);
        List<Point> closed = new ArrayList<>(points);
        closed.add(points.get(0));
        stroke.strokePolyline(closed);
        context.changed();
    }

    @Override
    public void pieSlice(int x, int y, int startAngle, int endAngle, int radius) {
        sector(x, y, startAngle, endAngle, radius, radius);
    }

    @Override
    public void sector(int x, int y, int startAngle, int endAngle, int radiusX, int radiusY) {
        List<Point> polygon = new ArrayList<>();
        polygon.add(new Point(x, y));
        polygon.addAll(EllipseRasterizer.arcVertices(x, y, radiusX, radiusY, startAngle, endAngle));
        PolygonRasterizer.fill(polygon, pattern);

        Point start = EllipseRasterizer.pointAt(x, y, radiusX, radiusY, startAngle);
        Point end = EllipseRasterizer.pointAt(x, y, radiusX, radiusY, endAngle);
        PixelSet outline = new PixelSet();
        stroke.addArc(outline, x, y, startAngle, endAngle, radiusX, radiusY);
        stroke.addSolidLine(outline, x, y, start.x(), start.y());
        stroke.addSolidLine(outline, x, y, end.x(), end.y());
        stroke.paint(outline);

        ShapeDrawer.rememberArc(context.state(), x, y, startAngle, endAngle, radiusX, radiusY);
        context.changed();
    }

    @Override
    public void floodFill(int x, int y, int borderColorCode) {
        int border = context.state().palette().toArgb(borderColorCode);
        FloodFillRasterizer.fill(context.canvas(), x, y, border, pattern);
        context.changed();
    }

    private void fillRect(Rect box) {
        for (int row = box.top(); row <= box.bottom(); row++) {
            pattern.accept(row, box.left(), box.right());
        }
    }
}
