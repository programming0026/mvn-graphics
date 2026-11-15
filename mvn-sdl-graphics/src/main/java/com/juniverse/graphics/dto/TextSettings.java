package com.juniverse.graphics.dto;

import com.juniverse.graphics.text.FontName;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.text.TextDirection;
import com.juniverse.graphics.text.VerticalAlignment;

/**
 * Configuración de texto actual (equivalente a {@code textsettingstype}).
 *
 * @param font       fuente
 * @param direction  dirección
 * @param charSize   factor de tamaño (1..10) o {@link #USER_CHAR_SIZE}
 * @param horizontal justificación horizontal
 * @param vertical   justificación vertical
 */
public record TextSettings(FontName font, TextDirection direction, int charSize,
                           HorizontalAlignment horizontal, VerticalAlignment vertical) {

    /** Tamaño definido por el usuario con {@code setusercharsize}. */
    public static final int USER_CHAR_SIZE = 0;

    /** Tamaño máximo admitido. */
    public static final int MAX_CHAR_SIZE = 10;

    /** Configuración por defecto de BGI. */
    public static final TextSettings DEFAULT = new TextSettings(FontName.DEFAULT, TextDirection.HORIZONTAL, 1,
            HorizontalAlignment.LEFT, VerticalAlignment.TOP);

    public TextSettings {
        if (font == null || direction == null || horizontal == null || vertical == null) {
            throw new IllegalArgumentException("Text settings must not contain null values");
        }
        charSize = Math.max(USER_CHAR_SIZE, Math.min(MAX_CHAR_SIZE, charSize));
    }

    public TextSettings withStyle(FontName newFont, TextDirection newDirection, int newCharSize) {
        return new TextSettings(newFont, newDirection, newCharSize, horizontal, vertical);
    }

    public TextSettings withJustification(HorizontalAlignment newHorizontal, VerticalAlignment newVertical) {
        return new TextSettings(font, direction, charSize, newHorizontal, newVertical);
    }
}
