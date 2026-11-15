package com.juniverse.graphics.drawing;

import com.juniverse.graphics.dto.Point;

import java.util.List;

/**
 * Operaciones de figuras rellenas con el patrón y color de relleno actuales.
 */
public interface FillOperations {

    /** Rellena un rectángulo sin dibujar su borde. */
    void bar(int left, int top, int right, int bottom);

    /** Dibuja una barra rellena con contorno tridimensional. */
    void bar3d(int left, int top, int right, int bottom, int depth, boolean drawTop);

    /** Dibuja y rellena una elipse completa. */
    void fillEllipse(int x, int y, int radiusX, int radiusY);

    /** Dibuja y rellena un polígono cerrado. */
    void fillPoly(List<Point> points);

    /** Dibuja y rellena un sector circular ("rebanada de pastel"). */
    void pieSlice(int x, int y, int startAngle, int endAngle, int radius);

    /** Dibuja y rellena un sector elíptico. */
    void sector(int x, int y, int startAngle, int endAngle, int radiusX, int radiusY);

    /** Rellena la región que contiene a (x, y) delimitada por el color de borde. */
    void floodFill(int x, int y, int borderColorCode);
}
