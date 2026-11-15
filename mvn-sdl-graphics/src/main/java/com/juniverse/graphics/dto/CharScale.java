package com.juniverse.graphics.dto;

/**
 * Factores de escala definidos con {@code setusercharsize}.
 * El ancho se multiplica por {@code multX/divX} y el alto por {@code multY/divY}.
 */
public record CharScale(int multX, int divX, int multY, int divY) {

    /** Escala neutra. */
    public static final CharScale IDENTITY = new CharScale(1, 1, 1, 1);

    public CharScale {
        if (multX <= 0 || divX <= 0 || multY <= 0 || divY <= 0) {
            throw new IllegalArgumentException("Scale factors must be positive");
        }
    }

    public double scaleX() {
        return (double) multX / divX;
    }

    public double scaleY() {
        return (double) multY / divY;
    }
}
