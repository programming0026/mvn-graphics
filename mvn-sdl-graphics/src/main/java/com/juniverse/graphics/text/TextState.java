package com.juniverse.graphics.text;

import com.juniverse.graphics.dto.CharScale;
import com.juniverse.graphics.dto.FontSpec;
import com.juniverse.graphics.dto.TextSettings;
import java.util.Objects;

/**
 * Estado de texto de una ventana: configuración y escala de usuario.
 */
public final class TextState {

    private TextSettings settings = TextSettings.DEFAULT;
    private CharScale charScale = CharScale.IDENTITY;

    /** Restaura los valores por defecto. */
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

    /** Fuente efectiva según la configuración actual. */
    public FontSpec fontSpec() {
        return FontSpec.from(settings, charScale);
    }
}
