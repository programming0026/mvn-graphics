package com.juniverse.graphics.text;

import com.juniverse.graphics.dto.CharScale;
import com.juniverse.graphics.dto.FontSpec;
import com.juniverse.graphics.dto.TextSettings;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BitmapFontTest {

    private final BitmapGlyphRasterizer rasterizer = new BitmapGlyphRasterizer();

    @Test
    public void defaultFontUsesEightPixelCellsTimesCharSize() {
        TextMask size1 = rasterizer.render("Hola", spec(1));
        TextMask size3 = rasterizer.render("Hola", spec(3));

        assertEquals(32, size1.width());
        assertEquals(8, size1.height());
        assertEquals(96, size3.width());
        assertEquals(24, size3.height());
    }

    @Test
    public void glyphPixelsMatchFontData() {
        // Fila 4 de la 'A' (0x3F): barra horizontal de las columnas 0 a 5.
        TextMask mask = rasterizer.render("A", spec(1));
        for (int x = 0; x <= 5; x++) {
            assertTrue(mask.isSet(x, 4));
        }
        assertFalse(mask.isSet(6, 4));
        assertFalse(mask.isSet(0, 7));
    }

    @Test
    public void scalingRepeatsPixels() {
        TextMask size1 = rasterizer.render("A", spec(1));
        TextMask size2 = rasterizer.render("A", spec(2));
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                assertEquals(size1.isSet(x, y), size2.isSet(2 * x + 1, 2 * y + 1));
            }
        }
    }

    @Test
    public void spanishCharactersHaveGlyphsAndUnknownFallsBackToQuestionMark() {
        assertFalse(java.util.Arrays.equals(BitmapFont.glyph('?'), BitmapFont.glyph('ñ')));
        assertFalse(java.util.Arrays.equals(BitmapFont.glyph('?'), BitmapFont.glyph('¿')));
        assertArrayEquals(BitmapFont.glyph('?'), BitmapFont.glyph('€'));
    }

    @Test
    public void everyCodePage437CharacterHasItsOwnGlyph() {
        for (int code = 0; code < 256; code++) {
            char character = CodePage437.toUnicode(code);
            assertTrue(BitmapFont.hasGlyph(character),
                    "Falta el glifo de " + character + " (código " + code + ")");
        }
    }

    @Test
    public void glyphsAreDrawnAsDescribed() {
        // Línea simple vertical: columnas 3 y 4 en las 8 filas (bit 0 = columna izquierda).
        for (int row : BitmapFont.glyph('│')) {
            assertEquals(0x18, row);
        }
        // Bloque completo y medio bloque inferior.
        assertArrayEquals(new int[]{0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF}, BitmapFont.glyph('█'));
        assertArrayEquals(new int[]{0, 0, 0, 0, 0xFF, 0xFF, 0xFF, 0xFF}, BitmapFont.glyph('▄'));
        // Las letras acentuadas conservan el cuerpo de la letra base (filas 2..7).
        int[] base = BitmapFont.glyph('o');
        int[] accented = BitmapFont.glyph('ô');
        for (int row = 2; row < 8; row++) {
            assertEquals(base[row], accented[row]);
        }
        assertFalse(java.util.Arrays.equals(BitmapFont.glyph('ô'), BitmapFont.glyph('ö')));
    }

    @Test
    public void routingRasterizerSelectsEngineByFont() {
        GlyphRasterizer fallback = (text, spec) -> TextMask.empty(99);
        FontRoutingRasterizer routing = new FontRoutingRasterizer(
                Map.of(FontName.DEFAULT, rasterizer), fallback);

        assertEquals(8, routing.render("x", spec(1)).height());
        assertEquals(99, routing.render("x", new FontSpec(FontName.TRIPLEX, 8, 1.0)).height());
        assertSame(FontRoutingRasterizer.class, FontRoutingRasterizer.standard().getClass());
    }

    private static FontSpec spec(int charSize) {
        return FontSpec.from(TextSettings.DEFAULT.withStyle(FontName.DEFAULT, TextDirection.HORIZONTAL, charSize),
                CharScale.IDENTITY);
    }
}
