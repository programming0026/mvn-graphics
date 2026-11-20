package com.juniverse.graphics.raster;

import com.juniverse.graphics.dto.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Rasterization of elliptical and circular arcs.
 * <p>
 * Angles are expressed in degrees with BGI's mathematical convention:
 * 0° points to the right and angles grow counterclockwise.
 */
public final class EllipseRasterizer {

    private EllipseRasterizer() {
    }

    /**
     * Generates the contiguous pixels of an elliptical arc.
     *
     * @param centerX    horizontal center
     * @param centerY    vertical center
     * @param radiusX    horizontal radius
     * @param radiusY    vertical radius
     * @param startAngle start angle in degrees
     * @param endAngle   end angle in degrees
     * @param sink       pixel receiver (may receive duplicates at the vertices)
     */
    public static void traceArc(int centerX, int centerY, int radiusX, int radiusY,
                                int startAngle, int endAngle, PointSink sink) {
        List<Point> vertices = arcVertices(centerX, centerY, radiusX, radiusY, startAngle, endAngle);
        Point previous = vertices.get(0);
        sink.accept(previous.x(), previous.y());
        for (int i = 1; i < vertices.size(); i++) {
            Point current = vertices.get(i);
            LineRasterizer.trace(previous.x(), previous.y(), current.x(), current.y(), sink);
            previous = current;
        }
    }

    /**
     * Computes the vertices of the polyline that approximates the arc. The vertex
     * density depends on the radius so that the result is continuous.
     */
    public static List<Point> arcVertices(int centerX, int centerY, int radiusX, int radiusY,
                                          int startAngle, int endAngle) {
        double start = startAngle;
        double end = normalizeEnd(startAngle, endAngle);
        int radius = Math.max(1, Math.max(Math.abs(radiusX), Math.abs(radiusY)));
        // Angular step of approximately one pixel along the outline.
        double stepDegrees = Math.max(0.25, Math.toDegrees(1.0 / radius));
        int steps = Math.max(1, (int) Math.ceil((end - start) / stepDegrees));

        List<Point> vertices = new ArrayList<>(steps + 1);
        for (int i = 0; i <= steps; i++) {
            double angle = start + (end - start) * i / steps;
            Point point = pointAt(centerX, centerY, radiusX, radiusY, angle);
            if (vertices.isEmpty() || !vertices.get(vertices.size() - 1).equals(point)) {
                vertices.add(point);
            }
        }
        return vertices;
    }

    /** Point of the ellipse corresponding to an angle (in degrees). */
    public static Point pointAt(int centerX, int centerY, int radiusX, int radiusY, double angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        int x = centerX + (int) Math.round(radiusX * Math.cos(radians));
        int y = centerY - (int) Math.round(radiusY * Math.sin(radians));
        return new Point(x, y);
    }

    /**
     * Generates the horizontal spans that fill a complete ellipse.
     */
    public static void fillEllipse(int centerX, int centerY, int radiusX, int radiusY, SpanSink sink) {
        int rx = Math.abs(radiusX);
        int ry = Math.abs(radiusY);
        if (ry == 0) {
            sink.accept(centerY, centerX - rx, centerX + rx);
            return;
        }
        for (int dy = -ry; dy <= ry; dy++) {
            double ratio = (double) dy / ry;
            int half = (int) Math.round(rx * Math.sqrt(Math.max(0.0, 1.0 - ratio * ratio)));
            sink.accept(centerY + dy, centerX - half, centerX + half);
        }
    }

    /**
     * Adjusts the end angle so that the sweep is counterclockwise from the
     * start. A sweep of 360° or more is limited to one full turn.
     */
    private static double normalizeEnd(int startAngle, int endAngle) {
        double end = endAngle;
        while (end < startAngle) {
            end += 360;
        }
        return Math.min(end, startAngle + 360.0);
    }
}
