package com.juniverse.graphics.text;

import com.juniverse.graphics.dto.FontSpec;

/**
 * Strategy to convert a text into a bitmap.
 * <p>
 * Allows replacing the font engine (AWT, custom bitmap fonts,
 * SDL_ttf, etc.) without modifying {@link TextDrawer}.
 */
public interface GlyphRasterizer {

    /** Rasterizes the text horizontally with the given font. */
    TextMask render(String text, FontSpec spec);
}
