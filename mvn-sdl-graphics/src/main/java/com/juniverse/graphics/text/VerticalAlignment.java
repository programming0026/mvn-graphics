package com.juniverse.graphics.text;

/**
 * Vertical text justification (BOTTOM_TEXT, VCENTER_TEXT, TOP_TEXT).
 * <p>
 * The order of the constants respects the BGI numeric codes.
 */
public enum VerticalAlignment {
    BOTTOM,
    CENTER,
    TOP;

    /** Offset to apply to the origin for a text of height {@code height}. */
    public int offset(int height) {
        return switch (this) {
            case TOP -> 0;
            case CENTER -> -height / 2;
            case BOTTOM -> -height;
        };
    }
}
