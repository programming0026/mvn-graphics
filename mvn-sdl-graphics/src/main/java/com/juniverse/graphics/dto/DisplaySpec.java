package com.juniverse.graphics.dto;

import java.util.Objects;

/**
 * Parámetros para crear una ventana nativa.
 *
 * @param width  ancho del área cliente en píxeles
 * @param height alto del área cliente en píxeles
 * @param title  título de la ventana
 * @param left   posición horizontal en pantalla, o {@link #CENTERED}
 * @param top    posición vertical en pantalla, o {@link #CENTERED}
 */
public record DisplaySpec(int width, int height, String title, int left, int top) {

    /** Valor de posición que centra la ventana en la pantalla. */
    public static final int CENTERED = Integer.MIN_VALUE;

    public DisplaySpec {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid window size: " + width + "x" + height);
        }
        Objects.requireNonNull(title, "Title must not be null");
    }

    /** Ventana centrada con el tamaño y título indicados. */
    public static DisplaySpec centered(int width, int height, String title) {
        return new DisplaySpec(width, height, title, CENTERED, CENTERED);
    }
}
