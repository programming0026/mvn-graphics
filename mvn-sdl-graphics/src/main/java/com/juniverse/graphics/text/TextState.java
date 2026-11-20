package com.juniverse.graphics.text;

import com.juniverse.graphics.dto.CharScale;
import com.juniverse.graphics.dto.FontSpec;
import com.juniverse.graphics.dto.TextSettings;
import java.util.Objects;

/**
 * Text state of a window: settings and user scale.
 */
public final class TextState {

    private TextSettings settings = TextSettings.DEFAULT;
    private CharScale charScale = CharScale.IDENTITY;

    /** Restores the default values. */
    public void reset() {
        settings = TextSettings.DEFAULT;
        charScale = CharScale.IDENTITY;
    }

    public TextSettings settings() {
        return settings;
    }

    public void setSettings(TextSettings settings) {
        this.settings = Objects.requireNonNull(settings);
    }

    public CharScale charScale() {
        return charScale;
    }

    public void setCharScale(CharScale charScale) {
        this.charScale = Objects.requireNonNull(charScale);
    }

    /** Effective font according to the current settings. */
    public FontSpec fontSpec() {
        return FontSpec.from(settings, charScale);
    }
}
