package com.juniverse.graphics.examples.bgidemo;

import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.text.CodePage437;
import com.juniverse.graphics.text.FontName;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.text.TextDirection;
import com.juniverse.graphics.text.VerticalAlignment;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Text demonstrations: {@code TextDump} and {@code TextDemo}.
 */
public final class TextDemos {

    private static final int[] CGA_SIZES = {1, 3, 7, 3, 3, 2, 2, 2, 2, 2, 2};
    private static final int[] NORMAL_SIZES = {1, 4, 7, 4, 4, 2, 2, 2, 2, 2, 2};
    private static final int[] DEMO_SIZES = {1, 3, 7, 3, 4, 2, 2, 2, 2, 2, 2};
    private static final int CHARACTER_COUNT = 256;
    private static final int LEFT_MARGIN = 2;
    private static final int TOP_MARGIN = 3;
    private static final int LINE_SPACING = 3;

    private final DemoFrame frame;

    public TextDemos(DemoFrame frame) {
        this.frame = frame;
    }

    /**
     * Shows all the characters of each font (TextDump).
     * <p>
     * Codes 0..255 are converted with {@link CodePage437}, to show
     * the same characters as in DOS (Ç ü é ... ░▒▓ ...). Converting them
     * directly to {@code char} would give Unicode control characters.
     * <p>
     * The original measured the letter width before changing font, so
     * with large fonts the lines ran off the right edge. Here each
     * character is measured with the current font and moved to the next line
     * before writing the one that no longer fits.
     */
    public void textDump() {
        for (FontName font : FontName.values()) {
            frame.mainWindow(DemoFrame.FONT_NAMES.get(font.ordinal()) + " Character Set");
            Viewport vp = getViewSettings();

            setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
            int windowWidth = vp.right() - vp.left();

            int firstCharacter;
            if (font == FontName.DEFAULT) {
                frame.changeTextStyle(font, TextDirection.HORIZONTAL, 1);
                firstCharacter = 0;
            } else {
                int size = frame.maxY() < 200 ? CGA_SIZES[font.ordinal()] : NORMAL_SIZES[font.ordinal()];
                frame.changeTextStyle(font, TextDirection.HORIZONTAL, size);
                firstCharacter = '!';
            }
            moveTo(LEFT_MARGIN, TOP_MARGIN);
            int lineHeight = textHeight("H") + LINE_SPACING;
            for (int code = firstCharacter; code < CHARACTER_COUNT; code++) {
                String character = String.valueOf(CodePage437.toUnicode(code));
                if (getX() + textWidth(character) > windowWidth) {
                    moveTo(LEFT_MARGIN, getY() + lineHeight);
                }
                outText(character);
            }
            frame.pause();
        }
    }

    /** Shows each font vertically, horizontally and in several sizes (TextDemo). */
    public void textDemo() {
        for (FontName font : FontName.values()) {
            frame.mainWindow(DemoFrame.FONT_NAMES.get(font.ordinal()) + " Demonstration");
            Viewport vp = getViewSettings();
            int demoSize = DEMO_SIZES[font.ordinal()];

            frame.changeTextStyle(font, TextDirection.VERTICAL, demoSize);
            setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
            outTextXY(2 * textWidth("M"), vp.bottom() - 2 * textHeight("M"), "Vertical");

            frame.changeTextStyle(font, TextDirection.HORIZONTAL, demoSize);
            setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
            outTextXY(2 * textWidth("M"), 2, "Horizontal");

            setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
            int x = (vp.right() - vp.left()) / 2;
            int y = textHeight("H");
            int h = 0;
            for (int i = 1; i < 5; i++) {
                int size = font == FontName.SMALL ? i + 3 : i;
                frame.changeTextStyle(font, TextDirection.HORIZONTAL, size);
                h = textHeight("H");
                y += h;
                outTextXY(x, y, "Size " + size);
            }

            if (font != FontName.DEFAULT) {
                y += h / 2;
                setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
                setUserCharSize(5, 6, 3, 2);
                frame.changeTextStyle(font, TextDirection.HORIZONTAL, USER_CHAR_SIZE);
                outTextXY((vp.right() - vp.left()) / 2, y, "User Defined Size");
            }
            frame.pause();
        }
    }
}
