package com.juniverse.graphics.dto;

import java.util.Objects;

/**
 * Parameters to create a native window.
 *
 * @param width  width of the client area in pixels
 * @param height height of the client area in pixels
 * @param title  window title
 * @param left   horizontal position on screen, or {@link #CENTERED}
 * @param top    vertical position on screen, or {@link #CENTERED}
 */
public record DisplaySpec(int width, int height, String title, int left, int top) {

    /** Position value that centers the window on the screen. */
    public static final int CENTERED = Integer.MIN_VALUE;

    public DisplaySpec {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid window size: " + width + "x" + height);
        }
        Objects.requireNonNull(title, "Title must not be null");
    }

    /** Window centered with the given size and title. */
    public static DisplaySpec centered(int width, int height, String title) {
        return new DisplaySpec(width, height, title, CENTERED, CENTERED);
    }
}
