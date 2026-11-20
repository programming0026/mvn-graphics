package com.juniverse.graphics.platform;

/**
 * How the BGI page fits the window when the window changes
 * size (for example, when maximized).
 */
public enum ScalingMode {
    /**
     * The drawing area grows with the window: when maximized, {@code getMaxX()}
     * and {@code getMaxY()} become the real size of the window and nothing is
     * scaled (text looks sharp). The area is never smaller than the
     * initial resolution; if the window is smaller, the image is shrunk
     * keeping the aspect ratio. What was already drawn is kept at the top
     * left. This is the default mode.
     */
    RESIZE,
    /**
     * Keeps the aspect ratio. The leftover space is filled with the current
     * background color (the one from {@code setBkColor}).
     */
    KEEP_ASPECT,
    /** Stretches the image to cover the whole window, even if it gets distorted. */
    STRETCH,
    /**
     * Enlarges only by integer factors (x2, x3, ...), so that each BGI
     * pixel takes exactly the same number of screen pixels. It is the
     * sharpest mode for text and lines; the rest is filled with the
     * background color.
     */
    INTEGER
}
