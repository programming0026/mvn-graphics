package com.juniverse.graphics.text;

import java.awt.Font;

/**
 * BGI fonts ({@code font_names}).
 * <p>
 * Borland's original vector fonts (.CHR) are replaced by the
 * Java logical fonts that most resemble them.
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

    /** Java logical font family. */
    public String family() {
        return family;
    }

    /** AWT style ({@link Font#PLAIN}, {@link Font#BOLD}, ...). */
    public int style() {
        return style;
    }

    /** Size in pixels for a scale factor of 1. */
    public int baseSize() {
        return baseSize;
    }

    /** Gets the font from its BGI numeric code. */
    public static FontName fromCode(int code) {
        if (code < 0 || code >= values().length) {
            throw new IllegalArgumentException("Invalid font: " + code);
        }
        return values()[code];
    }
}
