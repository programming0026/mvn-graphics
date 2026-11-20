package com.juniverse.graphics.text;

/**
 * Horizontal text justification (LEFT_TEXT, CENTER_TEXT, RIGHT_TEXT).
 */
public enum HorizontalAlignment {
    LEFT,
    CENTER,
    RIGHT;

    /** Offset to apply to the origin for a text of width {@code width}. */
    public int offset(int width) {
        return switch (this) {
            case LEFT -> 0;
            case CENTER -> -width / 2;
            case RIGHT -> -width;
        };
    }
}
