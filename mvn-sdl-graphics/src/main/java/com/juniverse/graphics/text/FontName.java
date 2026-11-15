package com.juniverse.graphics.text;

import java.awt.Font;

/**
 * Fuentes de BGI ({@code font_names}).
 * <p>
 * Las fuentes vectoriales originales de Borland (.CHR) se sustituyen por las
 * fuentes lógicas de Java que más se les parecen.
 */
public enum FontName {
    DEFAULT(Font.MONOSPACED, Font.BOLD, 8),
    TRIPLEX(Font.SERIF, Font.PLAIN, 8),
    SMALL(Font.SANS_SERIF, Font.PLAIN, 6),
    SANS_SERIF(Font.SANS_SERIF, Font.PLAIN, 8),
    GOTHIC(Font.SERIF, Font.BOLD, 8),
    SCRIPT(Font.SERIF, Font.ITALIC, 8),
    SIMPLEX(Font.SANS_SERIF, Font.PLAIN, 8),
    TRIPLEX_SCRIPT(Font.SERIF, Font.BOLD | Font.ITALIC, 8),
    COMPLEX(Font.SERIF, Font.PLAIN, 8),
    EUROPEAN(Font.SANS_SERIF, Font.BOLD, 8),
    BOLD(Font.SANS_SERIF, Font.BOLD, 8);

    private final String family;
    private final int style;
    private final int baseSize;

    private FontName(String family, int style, int baseSize) {
        this.family = family;
        this.style = style;
        this.baseSize = baseSize;
    }

    /** Familia de fuente lógica de Java. */
    public String family() {
        return family;
    }

    /** Estilo AWT ({@link Font#PLAIN}, {@link Font#BOLD}, ...). */
    public int style() {
        return style;
    }

    /** Tamaño en píxeles para un factor de escala 1. */
    public int baseSize() {
        return baseSize;
    }

    /** Obtiene la fuente a partir de su código numérico BGI. */
    public static FontName fromCode(int code) {
        if (code < 0 || code >= values().length) {
            throw new IllegalArgumentException("Invalid font: " + code);
        }
        return values()[code];
    }
}
