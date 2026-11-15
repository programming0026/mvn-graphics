package com.juniverse.graphics.raster;

import com.juniverse.graphics.dto.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Rasterizado de arcos elípticos y circulares.
 * <p>
 * Los ángulos se expresan en grados con la convención matemática de BGI:
 * 0° apunta a la derecha y los ángulos crecen en sentido antihorario.
 */
public final class EllipseRasterizer {

    private EllipseRasterizer() {
    }

    /**
     * Genera los píxeles contiguos de un arco elíptico.
     *
     * @param centerX    centro horizontal
     * @param centerY    centro vertical
     * @param radiusX    radio horizontal
     * @param radiusY    radio vertical
     * @param startAngle ángulo inicial en grados
     * @param endAngle   ángulo final en grados
     * @param sink       receptor de píxeles (puede recibir duplicados en los vértices)
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
     * Calcula los vértices de la poligonal que aproxima el arco. La densidad
     * de vértices depende del radio para que el resultado sea continuo.
     */
    public static List<Point> arcVertices(int centerX, int centerY, int radiusX, int radiusY,
                                          int startAngle, int endAngle) {
        double start = startAngle;
        double end = normalizeEnd(startAngle, endAngle);
        int radius = Math.max(1, Math.max(Math.abs(radiusX), Math.abs(radiusY)));
        // Paso angular de aproximadamente un píxel sobre el contorno.
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

    /** Punto de la elipse correspondiente a un ángulo (en grados). */
    public static Point pointAt(int centerX, int centerY, int radiusX, int radiusY, double angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        int x = centerX + (int) Math.round(radiusX * Math.cos(radians));
        int y = centerY - (int) Math.round(radiusY * Math.sin(radians));
        return new Point(x, y);
    }

    /**
     * Genera los tramos horizontales que rellenan una elipse completa.
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
     * Ajusta el ángulo final para que el recorrido sea antihorario desde el
     * inicial. Un recorrido de 360° o más se limita a una vuelta completa.
     */
    private static double normalizeEnd(int startAngle, int endAngle) {
        double end = endAngle;
        while (end < startAngle) {
            end += 360;
        }
        return Math.min(end, startAngle + 360.0);
    }
}
