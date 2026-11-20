package com.juniverse.graphics.style;

/**
 * Standard BGI line styles ({@code line_styles}).
 * <p>
 * Each style is described by a 16-bit pattern: a bit set to 1 draws the
 * pixel and a bit set to 0 skips it. The most significant bit corresponds to the
 * first pixel of the line.
 */
public enum LineStyle {
    SOLID(0xFFFF),
    DOTTED(0xCCCC),
    CENTER(0xFC78),
    DASHED(0xF8F8),
    /** Pattern defined by the user through {@code setlinestyle}. */
    USER_BIT(0x0000);

    private final int pattern;

    private LineStyle(int pattern) {
        this.pattern = pattern;
    }

    /** Predefined 16-bit pattern of the style. */
    public int pattern() {
        return pattern;
    }

    /** Gets the style from its BGI numeric code. */
    public static LineStyle fromCode(int code) {
        if (code < 0 || code >= values().length) {
            throw new IllegalArgumentException("Invalid line style: " + code);
        }
        return values()[code];
    }
}
