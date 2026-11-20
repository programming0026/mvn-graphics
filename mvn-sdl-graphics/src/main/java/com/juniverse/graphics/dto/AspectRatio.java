package com.juniverse.graphics.dto;

/**
 * Screen aspect ratio (equivalent to {@code getaspectratio}).
 */
public record AspectRatio(int x, int y) {

    /** Square ratio used by default (10000:10000, same as WinBGI). */
    public static final AspectRatio SQUARE = new AspectRatio(10_000, 10_000);

    public AspectRatio {
        if (x <= 0 || y <= 0) {
            throw new IllegalArgumentException("Aspect ratio must be positive");
        }
    }
}
