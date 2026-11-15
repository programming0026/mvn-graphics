package com.juniverse.graphics.dto;

/**
 * Relación de aspecto de la pantalla (equivalente a {@code getaspectratio}).
 */
public record AspectRatio(int x, int y) {

    /** Relación cuadrada usada por defecto (10000:10000, igual que WinBGI). */
    public static final AspectRatio SQUARE = new AspectRatio(10_000, 10_000);

    public AspectRatio {
        if (x <= 0 || y <= 0) {
            throw new IllegalArgumentException("Aspect ratio must be positive");
        }
    }
}
