package com.juniverse.graphics.text;

import com.juniverse.graphics.dto.FontSpec;

/**
 * Rasterizador basado en la fuente de mapa de bits {@link BitmapFont}.
 * <p>
 * Reproduce el comportamiento de la fuente por defecto de BGI: cada carácter
 * ocupa una celda de 8x8 píxeles multiplicada por el tamaño de carácter
 * (charSize 2 = 16x16, charSize 3 = 24x24, ...). La ampliación es por
 * repetición de píxeles, así que el texto siempre se ve nítido.
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
