package com.juniverse.graphics.examples.dto;

/**
 * Color box of the palette demonstration ({@code PaletteDemo}).
 *
 * @param left   left edge
 * @param top    top edge
 * @param right  right edge
 * @param bottom bottom edge
 * @param color  palette index it is painted with
 */
public record PaletteBox(int left, int top, int right, int bottom, int color) {
}
