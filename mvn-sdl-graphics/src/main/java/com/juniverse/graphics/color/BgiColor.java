package com.juniverse.graphics.color;

/**
 * Los 16 colores estándar de Borland BGI.
 * <p>
 * El valor ordinal de cada constante coincide con el código numérico original
 * (BLACK = 0 ... WHITE = 15), por lo que {@link #code()} puede usarse en
 * cualquier método que reciba un código de color.
 */
public enum BgiColor {
    BLACK(0x000000),
    BLUE(0x0000A8),
    GREEN(0x00A800),
    CYAN(0x00A8A8),
    RED(0xA80000),
    MAGENTA(0xA800A8),
    BROWN(0xA85400),
    LIGHT_GRAY(0xA8A8A8),
    DARK_GRAY(0x545454),
    LIGHT_BLUE(0x5454FC),
    LIGHT_GREEN(0x54FC54),
    LIGHT_CYAN(0x54FCFC),
    LIGHT_RED(0xFC5454),
    LIGHT_MAGENTA(0xFC54FC),
    YELLOW(0xFCFC54),
    WHITE(0xFFFFFF);

    /** Índice máximo de color BGI (equivale a MAXCOLORS). */
    public static final int MAX_COLOR = 15;

    private final int defaultRgb;

    private BgiColor(int defaultRgb) {
        this.defaultRgb = defaultRgb;
    }

    /** Código numérico BGI del color (0..15). */
    public int code() {
        return ordinal();
    }

    /** Valor RGB por defecto en formato 0xRRGGBB (paleta VGA). */
    public int defaultRgb() {
        return defaultRgb;
    }

    /**
     * Obtiene el color BGI a partir de su código.
     *
     * @throws IllegalArgumentException si el código no está entre 0 y 15
     */
    public static BgiColor fromCode(int code) {
        if (code < 0 || code > MAX_COLOR) {
            throw new IllegalArgumentException("Invalid BGI color code: " + code);
        }
        return values()[code];
    }
}
