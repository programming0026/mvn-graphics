package com.juniverse.graphics.dto;

/**
 * Key of the cache of rasterized texts: the text and the font it was
 * drawn with.
 *
 * @param text rasterized text
 * @param spec font used
 */
public record GlyphCacheKey(String text, FontSpec spec) {
}
