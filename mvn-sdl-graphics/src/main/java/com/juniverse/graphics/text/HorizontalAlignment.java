package com.juniverse.graphics.text;

/**
 * Justificación horizontal del texto (LEFT_TEXT, CENTER_TEXT, RIGHT_TEXT).
 */
public enum HorizontalAlignment {
    LEFT,
    CENTER,
    RIGHT;

    /** Desplazamiento a aplicar al origen para un texto de ancho {@code width}. */
    public int offset(int width) {
        return switch (this) {
            case LEFT -> 0;
            case CENTER -> -width / 2;
            case RIGHT -> -width;
        };
    }
}
