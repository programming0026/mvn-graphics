package com.juniverse.graphics.dto;

import com.juniverse.graphics.style.LineStyle;

/**
 * Current line settings (equivalent to {@code linesettingstype}).
 *
 * @param style       line style
 * @param userPattern 16-bit pattern used when the style is {@link LineStyle#USER_BIT}
 * @param thickness   thickness in pixels ({@link #NORMAL_WIDTH} or {@link #THICK_WIDTH})
 */
public record LineSettings(LineStyle style, int userPattern, int thickness) {

    /** Normal thickness (NORM_WIDTH). */
    public static final int NORMAL_WIDTH = 1;

    /** Thick thickness (THICK_WIDTH). */
    public static final int THICK_WIDTH = 3;

    /** Default settings: solid one-pixel line. */
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

    /** 16-bit pattern that is actually applied when drawing. */
    public int effectivePattern() {
        return style == LineStyle.USER_BIT ? userPattern : style.pattern();
    }
}
