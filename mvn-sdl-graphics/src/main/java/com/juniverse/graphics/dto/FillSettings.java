package com.juniverse.graphics.dto;

import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.color.BgiColor;

/**
 * Current fill settings (equivalent to {@code fillsettingstype}).
 *
 * @param pattern fill pattern
 * @param color   fill color code
 */
public record FillSettings(FillPattern pattern, int color) {

    /** Default fill: solid white. */
    public static final FillSettings DEFAULT = new FillSettings(FillPattern.SOLID, BgiColor.WHITE.code());

    public FillSettings {
        if (pattern == null) {
            throw new IllegalArgumentException("Fill pattern must not be null");
        }
    }
}
