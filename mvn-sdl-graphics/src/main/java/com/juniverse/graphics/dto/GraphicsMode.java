package com.juniverse.graphics.dto;

import com.juniverse.graphics.driver.GraphicsDriver;

/**
 * BGI graphics mode: combination of driver, mode number and resolution.
 *
 * @param driver driver the mode belongs to
 * @param number BGI mode number (for example {@code VGAHI = 2})
 * @param label  short name of the mode (for example {@code "VGAHI"})
 * @param width  width in pixels
 * @param height height in pixels
 */
public record GraphicsMode(GraphicsDriver driver, int number, String label, int width, int height) {

    /** Descriptive name of the mode (equivalent to {@code getmodename}). */
    public String description() {
        return width + " x " + height + " " + label;
    }
}
