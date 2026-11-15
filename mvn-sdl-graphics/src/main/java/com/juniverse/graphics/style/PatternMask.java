package com.juniverse.graphics.style;

import java.util.Arrays;

/**
 * Máscara de relleno inmutable de 8x8 píxeles.
 * <p>
 * Cada fila es un byte; el bit más significativo corresponde a la columna
 * izquierda. Un bit en 1 se pinta con el color de relleno y un bit en 0 con
 * el color de fondo, tal como en BGI.
 */
public final class PatternMask {

    /** Tamaño de la máscara (8x8). */
    public static final int SIZE = 8;

    private final int[] rows;

    private PatternMask(int[] rows) {
        this.rows = rows;
    }

    /**
     * Crea una máscara a partir de 8 filas (solo se usan los 8 bits inferiores).
     */
    public static PatternMask of(int... rows) {
        if (rows.length != SIZE) {
            throw new IllegalArgumentException("A fill pattern requires " + SIZE + " rows");
        }
        return new PatternMask(Arrays.stream(rows).map(row -> row & 0xFF).toArray());
    }

    /** Indica si el bit correspondiente al píxel (x, y) está activo. */
    public boolean isSet(int x, int y) {
        return (rows[y & 7] & (0x80 >> (x & 7))) != 0;
    }

    /** Copia de las filas (equivale a {@code getfillpattern}). */
    public int[] rows() {
        return rows.clone();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PatternMask mask && Arrays.equals(rows, mask.rows);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(rows);
    }

    @Override
    public String toString() {
        return "PatternMask" + Arrays.toString(rows);
    }
}
