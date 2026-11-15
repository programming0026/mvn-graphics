package com.juniverse.graphics.drawing;

import com.juniverse.graphics.dto.Point;

import java.util.List;

/**
 * Operaciones de píxeles y contornos (figuras sin relleno).
 */
public interface ShapeOperations {

    /** Dibuja un píxel con el código de color indicado. */
    void putPixel(int x, int y, int colorCode);

    /** Lee el código de color de un píxel (0 si está fuera del área dibujable). */
    int getPixel(int x, int y);

    /** Dibuja el contorno de un rectángulo con el estilo de línea actual. */
    void rectangle(int left, int top, int right, int bottom);

    /** Dibuja una circunferencia. */
    void circle(int x, int y, int radius);

    /** Dibuja un arco circular entre dos ángulos (en grados). */
    void arc(int x, int y, int startAngle, int endAngle, int radius);

    /** Dibuja un arco elíptico entre dos ángulos (en grados). */
    void ellipse(int x, int y, int startAngle, int endAngle, int radiusX, int radiusY);

    /** Dibuja una polilínea abierta con los vértices indicados. */
    void drawPoly(List<Point> points);
}
