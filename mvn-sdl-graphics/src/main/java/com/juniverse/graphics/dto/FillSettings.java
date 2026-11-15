package com.juniverse.graphics.dto;

import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.color.BgiColor;

/**
 * Configuración de relleno actual (equivalente a {@code fillsettingstype}).
 *
 * @param pattern patrón de relleno
 * @param color   código de color del relleno
 */
public record FillSettings(FillPattern pattern, int color) {

    /** Relleno por defecto: sólido en blanco. */
    public static final FillSettings DEFAULT = new FillSettings(FillPattern.SOLID, BgiColor.WHITE.code());

    public FillSettings {
        if (pattern == null) {
            throw new IllegalArgumentException("Fill pattern must not be null");
        }
    }
}
