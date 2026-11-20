package com.juniverse.graphics.dto;

/**
 * Behavior options of a graphics window.
 *
 * @param doubleBuffered if true, drawing goes to page 1 while
 *                       page 0 is displayed (use {@code swapBuffers} to alternate)
 * @param exitOnClose    if true, closing the window terminates the program
 */
public record WindowOptions(boolean doubleBuffered, boolean exitOnClose) {

    /** Default {@code initwindow} options: no double buffering and exit on close. */
    public static final WindowOptions DEFAULT = new WindowOptions(false, true);
}
