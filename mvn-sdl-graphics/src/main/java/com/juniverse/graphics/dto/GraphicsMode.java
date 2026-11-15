package com.juniverse.graphics.dto;

import com.juniverse.graphics.driver.GraphicsDriver;

/**
 * Modo gráfico de BGI: combinación de controlador, número de modo y resolución.
 *
 * @param driver controlador al que pertenece el modo
 * @param number número de modo BGI (por ejemplo {@code VGAHI = 2})
 * @param label  nombre corto del modo (por ejemplo {@code "VGAHI"})
 * @param width  ancho en píxeles
 * @param height alto en píxeles
 */
public record GraphicsMode(GraphicsDriver driver, int number, String label, int width, int height) {

    /** Nombre descriptivo del modo (equivale a {@code getmodename}). */
    public String description() {
        return width + " x " + height + " " + label;
    }
}
