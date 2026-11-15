package com.juniverse.graphics.dto;

/**
 * Punto inmutable en coordenadas enteras de pantalla.
 *
 * @param x coordenada horizontal (crece hacia la derecha)
 * @param y coordenada vertical (crece hacia abajo)
 */
public record Point(int x, int y) {

    /** Punto que representa "sin clic" (equivalente a {@code NO_CLICK}). */
    public static final Point NONE = new Point(-1, -1);

    /** Devuelve un nuevo punto desplazado. */
    public Point translate(int dx, int dy) {
        return new Point(x + dx, y + dy);
    }
}
