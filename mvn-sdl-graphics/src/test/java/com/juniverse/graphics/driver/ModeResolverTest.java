package com.juniverse.graphics.driver;

import com.juniverse.graphics.dto.GraphicsMode;
import com.juniverse.graphics.dto.ModeResolution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ModeResolverTest {

    @Test
    public void detectIgnoresModeAndSelectsVgaHigh() {
        // Equivale a: int gd = DETECT, gm = VGA; initgraph(&gd, &gm, "");
        ModeResolution result = ModeResolver.resolve(GraphicsDriver.DETECT.code(), GraphicsDriver.VGA.code());

        GraphicsMode mode = assertInstanceOf(ModeResolution.Resolved.class, result).mode();
        assertEquals(GraphicsDriver.VGA, mode.driver());
        assertEquals(2, mode.number());
        assertEquals(640, mode.width());
        assertEquals(480, mode.height());
    }

    @Test
    public void explicitDriverUsesModeResolution() {
        ModeResolution result = ModeResolver.resolve(GraphicsDriver.EGA.code(), 1);

        GraphicsMode mode = assertInstanceOf(ModeResolution.Resolved.class, result).mode();
        assertEquals("640 x 350 EGAHI", mode.description());
    }

    @Test
    public void invalidDriverOrModeFails() {
        assertEquals(ModeFailureReason.INVALID_DRIVER,
                assertInstanceOf(ModeResolution.Failed.class, ModeResolver.resolve(99, 0)).reason());
        assertEquals(ModeFailureReason.INVALID_MODE,
                assertInstanceOf(ModeResolution.Failed.class,
                        ModeResolver.resolve(GraphicsDriver.VGA.code(), 7)).reason());
    }

    @Test
    public void modeRangesMatchBorland() {
        assertArrayEquals(new int[]{0, 2},
                new int[]{GraphicsDriver.VGA.lowestModeNumber(), GraphicsDriver.VGA.highestModeNumber()});
        assertArrayEquals(new int[]{3, 3},
                new int[]{GraphicsDriver.EGAMONO.lowestModeNumber(), GraphicsDriver.EGAMONO.highestModeNumber()});
        assertEquals("EGAVGA", GraphicsDriver.VGA.fileName());
    }
}
