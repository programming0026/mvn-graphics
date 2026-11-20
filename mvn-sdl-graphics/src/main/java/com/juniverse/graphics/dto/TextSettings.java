package com.juniverse.graphics.dto;

import com.juniverse.graphics.text.FontName;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.text.TextDirection;
import com.juniverse.graphics.text.VerticalAlignment;

/**
 * Current text settings (equivalent to {@code textsettingstype}).
 *
 * @param font       font
 * @param direction  direction
 * @param charSize   size factor (1..10) or {@link #USER_CHAR_SIZE}
 * @param horizontal horizontal justification
 * @param vertical   vertical justification
 */
public record TextSettings(FontName font, TextDirection direction, int charSize,
                           HorizontalAlignment horizontal, VerticalAlignment vertical) {

    /** Size defined by the user with {@code setusercharsize}. */
    public static final int USER_CHAR_SIZE = 0;

    /** Maximum allowed size. */
    public static final int MAX_CHAR_SIZE = 10;

    /** BGI default settings. */
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
