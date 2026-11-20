package com.juniverse.graphics.color;

/**
 * The 16 standard Borland BGI colors.
 * <p>
 * The ordinal value of each constant matches the original numeric code
 * (BLACK = 0 ... WHITE = 15), so {@link #code()} can be used in
 * any method that receives a color code.
 */
public enum BgiColor {
    BLACK(0x000000),
    BLUE(0x0000A8),
    GREEN(0x00A800),
    CYAN(0x00A8A8),
    RED(0xA80000),
    MAGENTA(0xA800A8),
    BROWN(0xA85400),
    LIGHT_GRAY(0xA8A8A8),
    DARK_GRAY(0x545454),
    LIGHT_BLUE(0x5454FC),
    LIGHT_GREEN(0x54FC54),
    LIGHT_CYAN(0x54FCFC),
    LIGHT_RED(0xFC5454),
    LIGHT_MAGENTA(0xFC54FC),
    YELLOW(0xFCFC54),
    WHITE(0xFFFFFF);

    /** Maximum BGI color index (equivalent to MAXCOLORS). */
    public static final int MAX_COLOR = 15;

    private final int defaultRgb;

    private BgiColor(int defaultRgb) {
        this.defaultRgb = defaultRgb;
    }

    /** BGI numeric code of the color (0..15). */
    public int code() {
        return ordinal();
    }

    /** Default RGB value in 0xRRGGBB format (VGA palette). */
    public int defaultRgb() {
        return defaultRgb;
    }

    /**
     * Gets the BGI color from its code.
     *
     * @throws IllegalArgumentException if the code is not between 0 and 15
     */
    public static BgiColor fromCode(int code) {
        if (code < 0 || code > MAX_COLOR) {
            throw new IllegalArgumentException("Invalid BGI color code: " + code);
        }
        return values()[code];
    }
}
