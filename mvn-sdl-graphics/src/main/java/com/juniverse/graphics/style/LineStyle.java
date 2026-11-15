package com.juniverse.graphics.style;

/**
 * Estilos de línea estándar de BGI ({@code line_styles}).
 * <p>
 * Cada estilo se describe con un patrón de 16 bits: un bit en 1 dibuja el
 * píxel y un bit en 0 lo omite. El bit más significativo corresponde al
 * primer píxel de la línea.
 */
public enum LineStyle {
    SOLID(0xFFFF),
    DOTTED(0xCCCC),
    CENTER(0xFC78),
    DASHED(0xF8F8),
    /** Patrón definido por el usuario mediante {@code setlinestyle}. */
    USER_BIT(0x0000);

    private final int pattern;

    private LineStyle(int pattern) {
        this.pattern = pattern;
    }

    /** Patrón de 16 bits predefinido del estilo. */
    public int pattern() {
        return pattern;
    }

    /** Obtiene el estilo a partir de su código numérico BGI. */
    public static LineStyle fromCode(int code) {
        if (code < 0 || code >= values().length) {
            throw new IllegalArgumentException("Invalid line style: " + code);
        }
        return values()[code];
    }
}
