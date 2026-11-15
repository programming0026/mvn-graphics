package com.juniverse.graphics.dto;

/**
 * Rectángulo inmutable definido por sus bordes (inclusivos).
 */
public record Rect(int left, int top, int right, int bottom) {

    /** Devuelve el rectángulo con las esquinas ordenadas (left &lt;= right, top &lt;= bottom). */
    public Rect normalized() {
        return new Rect(Math.min(left, right), Math.min(top, bottom),
                Math.max(left, right), Math.max(top, bottom));
    }

    /** Ancho en píxeles (bordes inclusivos). */
    public int width() {
        return Math.abs(right - left) + 1;
    }

    /** Alto en píxeles (bordes inclusivos). */
    public int height() {
        return Math.abs(bottom - top) + 1;
    }

    /** Indica si el punto está dentro del rectángulo. */
    public boolean contains(int x, int y) {
        Rect r = normalized();
        return x >= r.left && x <= r.right && y >= r.top && y <= r.bottom;
    }
}
