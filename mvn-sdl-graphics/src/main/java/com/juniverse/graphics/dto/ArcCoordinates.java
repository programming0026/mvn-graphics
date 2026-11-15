package com.juniverse.graphics.dto;

/**
 * Información del último arco dibujado (equivalente a {@code arccoordstype}).
 *
 * @param x      centro horizontal del arco
 * @param y      centro vertical del arco
 * @param xStart coordenada x del punto inicial
 * @param yStart coordenada y del punto inicial
 * @param xEnd   coordenada x del punto final
 * @param yEnd   coordenada y del punto final
 */
public record ArcCoordinates(int x, int y, int xStart, int yStart, int xEnd, int yEnd) {

    /** Valor inicial antes de dibujar cualquier arco. */
    public static final ArcCoordinates EMPTY = new ArcCoordinates(0, 0, 0, 0, 0, 0);
}
