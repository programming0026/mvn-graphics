package com.juniverse.graphics.dto;

import com.juniverse.graphics.style.LineStyle;

/**
 * Configuración de línea actual (equivalente a {@code linesettingstype}).
 *
 * @param style       estilo de la línea
 * @param userPattern patrón de 16 bits usado cuando el estilo es {@link LineStyle#USER_BIT}
 * @param thickness   grosor en píxeles ({@link #NORMAL_WIDTH} o {@link #THICK_WIDTH})
 */
public record LineSettings(LineStyle style, int userPattern, int thickness) {

    /** Grosor normal (NORM_WIDTH). */
    public static final int NORMAL_WIDTH = 1;

    /** Grosor grueso (THICK_WIDTH). */
    public static final int THICK_WIDTH = 3;

    /** Configuración por defecto: línea sólida de un píxel. */
    public static final LineSettings DEFAULT = new LineSettings(LineStyle.SOLID, 0xFFFF, NORMAL_WIDTH);

    public LineSettings {
        if (style == null) {
            throw new IllegalArgumentException("Line style must not be null");
        }
        if (thickness < 1) {
            throw new IllegalArgumentException("Thickness must be positive: " + thickness);
        }
        userPattern &= 0xFFFF;
    }

    /** Patrón de 16 bits que realmente se aplica al dibujar. */
    public int effectivePattern() {
        return style == LineStyle.USER_BIT ? userPattern : style.pattern();
    }
}
