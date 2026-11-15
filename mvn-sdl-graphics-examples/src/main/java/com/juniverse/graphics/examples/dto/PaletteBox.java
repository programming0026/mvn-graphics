package com.juniverse.graphics.examples.dto;

/**
 * Caja de color de la demostración de paleta ({@code PaletteDemo}).
 *
 * @param left   borde izquierdo
 * @param top    borde superior
 * @param right  borde derecho
 * @param bottom borde inferior
 * @param color  índice de la paleta con el que se pinta
 */
public record PaletteBox(int left, int top, int right, int bottom, int color) {
}
