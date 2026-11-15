package com.juniverse.graphics.dto;

/**
 * Clave de la caché de textos rasterizados: el texto y la fuente con la que
 * se dibujó.
 *
 * @param text texto rasterizado
 * @param spec fuente usada
 */
public record GlyphCacheKey(String text, FontSpec spec) {
}
