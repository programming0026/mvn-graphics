package com.juniverse.graphics.color;

import com.juniverse.graphics.style.RasterOperation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaletteTest {

    @Test
    public void bgiColorsMapToDefaultRgb() {
        Palette palette = new Palette();
        assertEquals(0xFFA80000, palette.toArgb(BgiColor.RED.code()));
        assertEquals(BgiColor.RED.code(), palette.toColorCode(0xFFA80000));
    }

    @Test
    public void rgbCodesRoundTrip() {
        Palette palette = new Palette();
        int code = ColorCode.rgb(1, 2, 3);
        assertTrue(ColorCode.isRgbColor(code));
        assertFalse(ColorCode.isBgiColor(code));
        assertEquals(0xFF010203, palette.toArgb(code));
        assertEquals(code, palette.toColorCode(0xFF010203));
    }

    @Test
    public void setRgbChangesEntry() {
        Palette palette = new Palette();
        palette.setRgb(1, 10, 20, 30);
        assertEquals(0x0A141E, palette.toRgb(1));
        palette.reset();
        assertEquals(BgiColor.BLUE.defaultRgb(), palette.toRgb(1));
    }

    @Test
    public void xorTwiceRestoresOriginal() {
        int destination = 0xFF123456;
        int source = 0xFFFFFFFF;
        int once = RasterOperation.XOR.apply(destination, source);
        assertEquals(destination, RasterOperation.XOR.apply(once, source));
    }
}
