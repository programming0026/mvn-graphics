package com.juniverse.graphics.text;

import com.juniverse.graphics.dto.FontSpec;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Composite rasterizer that chooses the engine according to the font: some fonts
 * have their own rasterizer (for example, {@link FontName#DEFAULT} with the
 * bitmap font) and the rest use a general one.
 * <p>
 * It allows adding engines for new fonts without modifying the
 * existing classes (open/closed principle).
 */
public final class FontRoutingRasterizer implements GlyphRasterizer {

    private final Map<FontName, GlyphRasterizer> routes;
    private final GlyphRasterizer fallback;

    /**
     * @param routes   specific rasterizer for each font
     * @param fallback rasterizer for fonts without their own route
     */
    public FontRoutingRasterizer(Map<FontName, GlyphRasterizer> routes, GlyphRasterizer fallback) {
        this.routes = routes.isEmpty() ? new EnumMap<>(FontName.class) : new EnumMap<>(routes);
        this.fallback = Objects.requireNonNull(fallback);
    }

    /** Standard configuration: default font as a bitmap and the rest with AWT. */
    public static FontRoutingRasterizer standard() {
        return new FontRoutingRasterizer(Map.of(FontName.DEFAULT, new BitmapGlyphRasterizer()),
                new AwtGlyphRasterizer());
    }

    @Override
    public TextMask render(String text, FontSpec spec) {
        return routes.getOrDefault(spec.font(), fallback).render(text, spec);
    }
}
