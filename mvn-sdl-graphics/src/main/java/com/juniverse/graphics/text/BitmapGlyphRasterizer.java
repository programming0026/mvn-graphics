package com.juniverse.graphics.text;

import com.juniverse.graphics.dto.FontSpec;

/**
 * Rasterizer based on the {@link BitmapFont} bitmap font.
 * <p>
 * Reproduces the behavior of BGI's default font: each character
 * takes an 8x8-pixel cell multiplied by the character size
 * (charSize 2 = 16x16, charSize 3 = 24x24, ...). Enlargement is by
 * pixel repetition, so the text always looks sharp.
 */
public final class BitmapGlyphRasterizer implements GlyphRasterizer {

    @Override
    public TextMask render(String text, FontSpec spec) {
        double scaleY = spec.pixelSize() / BitmapFont.GLYPH_SIZE;
        double scaleX = scaleY * spec.stretchX();
        int cellWidth = Math.max(1, (int) Math.round(BitmapFont.GLYPH_SIZE * scaleX));
        int cellHeight = Math.max(1, (int) Math.round(BitmapFont.GLYPH_SIZE * scaleY));
        if (text.isEmpty()) {
            return TextMask.empty(cellHeight);
        }

        int width = cellWidth * text.length();
        boolean[] bits = new boolean[width * cellHeight];
        for (int index = 0; index < text.length(); index++) {
            int[] glyph = BitmapFont.glyph(text.charAt(index));
            int offsetX = index * cellWidth;
            for (int y = 0; y < cellHeight; y++) {
                int sourceY = y * BitmapFont.GLYPH_SIZE / cellHeight;
                for (int x = 0; x < cellWidth; x++) {
                    int sourceX = x * BitmapFont.GLYPH_SIZE / cellWidth;
                    bits[y * width + offsetX + x] = BitmapFont.isSet(glyph, sourceX, sourceY);
                }
            }
        }
        return new TextMask(width, cellHeight, bits);
    }
}
