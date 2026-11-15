package com.juniverse.graphics.text;

import com.juniverse.graphics.dto.FontSpec;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CodePage437Test {

    @Test
    public void mapsDosCodesToTheCharactersShownOnScreen() {
        assertEquals('A', CodePage437.toUnicode('A'));
        assertEquals('☺', CodePage437.toUnicode(0x01));
        assertEquals('⌂', CodePage437.toUnicode(0x7F));
        assertEquals('Ç', CodePage437.toUnicode(0x80));
        assertEquals('ñ', CodePage437.toUnicode(0xA4));
        assertEquals('¿', CodePage437.toUnicode(0xA8));
        assertEquals('░', CodePage437.toUnicode(0xB0));
        assertEquals('█', CodePage437.toUnicode(0xDB));
        assertEquals('■', CodePage437.toUnicode(0xFE));
        // Igual que un char de C: solo cuentan los 8 bits inferiores.
        assertEquals('Ç', CodePage437.toUnicode((byte) 0x80));
        assertEquals("Ñandú", CodePage437.decode(new byte[]{(byte) 0xA5, 'a', 'n', 'd', (byte) 0xA3}));
    }

    @Test
    public void everyCodeHasAVisibleUniqueCharacter() {
        Set<Character> characters = new HashSet<>();
        for (int code = 1; code < 0xFF; code++) {
            char character = CodePage437.toUnicode(code);
            assertFalse(Character.isISOControl(character), "Código " + code + " es de control");
            characters.add(character);
        }
        assertEquals(0xFE, characters.size());
    }

    @Test
    public void awtRasterizerDrawsControlCharactersAsQuestionMark() {
        AwtGlyphRasterizer rasterizer = new AwtGlyphRasterizer();
        FontSpec spec = new FontSpec(FontName.TRIPLEX, 24, 1.0);

        TextMask control = rasterizer.render("\u0085", spec);
        TextMask question = rasterizer.render("?", spec);

        assertEquals(question.width(), control.width());
        assertArrayEquals(bits(question), bits(control));
        assertNotEquals(0, question.width());
        assertTrue(rasterizer.render("ñ", spec).width() > 0);
    }

    private static boolean[] bits(TextMask mask) {
        boolean[] bits = new boolean[mask.width() * mask.height()];
        for (int y = 0; y < mask.height(); y++) {
            for (int x = 0; x < mask.width(); x++) {
                bits[y * mask.width() + x] = mask.isSet(x, y);
            }
        }
        return bits;
    }
}
