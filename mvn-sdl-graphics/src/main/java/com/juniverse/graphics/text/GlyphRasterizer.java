package com.juniverse.graphics.text;

import com.juniverse.graphics.dto.FontSpec;

/**
 * Estrategia para convertir un texto en un mapa de bits.
 * <p>
 * Permite sustituir el motor de fuentes (AWT, fuentes bitmap propias,
 * SDL_ttf, etc.) sin modificar {@link TextDrawer}.
 */
public interface GlyphRasterizer {

    /** Rasteriza el texto horizontalmente con la fuente indicada. */
    TextMask render(String text, FontSpec spec);
}
