package com.juniverse.graphics.examples.mathdemo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link Plot2D}. It is in the same package because
 * {@code formatNumber} is visible only inside it.
 */
public class Plot2DTest {

    @Test
    public void axisLabelsUseThreeSignificantDigits() {
        assertEquals("-20", Plot2D.formatNumber(-20.0));
        assertEquals("4", Plot2D.formatNumber(4.0));
        assertEquals("0", Plot2D.formatNumber(1e-12));
        assertEquals("12.3", Plot2D.formatNumber(12.345));
    }
}
