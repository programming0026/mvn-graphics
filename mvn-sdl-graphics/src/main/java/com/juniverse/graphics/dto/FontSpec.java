package com.juniverse.graphics.dto;

import com.juniverse.graphics.text.FontName;

/**
 * Concrete description of the font a text is rasterized with.
 *
 * @param font      BGI font
 * @param pixelSize font size in pixels
 * @param stretchX  horizontal stretch factor (1 = natural proportion)
 */
public record FontSpec(FontName font, float pixelSize, double stretchX) {

    /** Base size used as reference by {@code USER_CHAR_SIZE} (equivalent to charSize 4). */
    private static final int USER_REFERENCE_SIZE = 4;

    public FontSpec {
        if (pixelSize <= 0 || stretchX <= 0) {
            throw new IllegalArgumentException("Invalid font size");
        }
    }

    /** Computes the specification from the current text settings. */
    public static FontSpec from(TextSettings settings, CharScale scale) {
        FontName font = settings.font();
        if (settings.charSize() == TextSettings.USER_CHAR_SIZE) {
            float size = (float) (font.baseSize() * USER_REFERENCE_SIZE * scale.scaleY());
            return new FontSpec(font, Math.max(1f, size), scale.scaleX() / scale.scaleY());
        }
        return new FontSpec(font, font.baseSize() * (float)settings.charSize(), 1.0);
    }
}
