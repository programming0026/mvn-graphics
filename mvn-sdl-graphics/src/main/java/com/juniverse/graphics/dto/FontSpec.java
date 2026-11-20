package com.juniverse.graphics.dto;

import com.juniverse.graphics.text.FontName;

/**
 * Descripción concreta de la fuente con la que se rasteriza un texto.
 *
 * @param font      fuente BGI
 * @param pixelSize tamaño de la fuente en píxeles
 * @param stretchX  factor de estiramiento horizontal (1 = proporción natural)
 */
public record FontSpec(FontName font, float pixelSize, double stretchX) {

    /** Tamaño base usado como referencia por {@code USER_CHAR_SIZE} (equivale a charSize 4). */
    private static final int USER_REFERENCE_SIZE = 4;

    public FontSpec {
        if (pixelSize <= 0 || stretchX <= 0) {
            throw new IllegalArgumentException("Invalid font size");
        }
    }

    /** Calcula la especificación a partir de la configuración de texto actual. */
    public static FontSpec from(TextSettings settings, CharScale scale) {
        FontName font = settings.font();
        if (settings.charSize() == TextSettings.USER_CHAR_SIZE) {
            float size = (float) (font.baseSize() * USER_REFERENCE_SIZE * scale.scaleY());
            return new FontSpec(font, Math.max(1f, size), scale.scaleX() / scale.scaleY());
        }
        return new FontSpec(font, font.baseSize() * (float)settings.charSize(), 1.0);
    }
}
