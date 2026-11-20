package com.juniverse.graphics.text;

import com.juniverse.graphics.dto.TextSettings;

/**
 * BGI text operations.
 */
public interface TextOperations {

    /** Draws the text at the current position (and advances it if the horizontal justification is LEFT). */
    void outText(String text);

    /** Draws the text at the given position without modifying the current position. */
    void outTextXY(int x, int y, String text);

    /** Width in pixels the text would take with the current settings. */
    int textWidth(String text);

    /** Height in pixels the text would take with the current settings. */
    int textHeight(String text);

    /** Changes font, direction and size. */
    void setTextStyle(FontName font, TextDirection direction, int charSize);

    /** Changes the justification. */
    void setTextJustify(HorizontalAlignment horizontal, VerticalAlignment vertical);

    /** Defines the user scale used with {@code USER_CHAR_SIZE}. */
    void setUserCharSize(int multX, int divX, int multY, int divY);

    /** Current text settings. */
    TextSettings textSettings();
}
