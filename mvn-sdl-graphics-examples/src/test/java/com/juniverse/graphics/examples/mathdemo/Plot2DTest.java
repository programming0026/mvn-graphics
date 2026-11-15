package com.juniverse.graphics.examples.mathdemo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas de {@link Plot2D}. Está en el mismo paquete porque
 * {@code formatNumber} es visible solo dentro de él.
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
