package com.juniverse.graphics.text;

import com.juniverse.graphics.dto.FontSpec;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Rasterizador compuesto que elige el motor según la fuente: algunas fuentes
 * tienen un rasterizador propio (por ejemplo, {@link FontName#DEFAULT} con la
 * fuente de mapa de bits) y el resto usa uno general.
 * <p>
 * Permite agregar motores para nuevas fuentes sin modificar las clases
 * existentes (principio abierto/cerrado).
 */
public final class FontRoutingRasterizer implements GlyphRasterizer {

    private final Map<FontName, GlyphRasterizer> routes;
    private final GlyphRasterizer fallback;

    /**
     * @param routes   rasterizador específico para cada fuente
     * @param fallback rasterizador para las fuentes sin ruta propia
     */
    public FontRoutingRasterizer(Map<FontName, GlyphRasterizer> routes, GlyphRasterizer fallback) {
        this.routes = routes.isEmpty() ? new EnumMap<>(FontName.class) : new EnumMap<>(routes);
        this.fallback = Objects.requireNonNull(fallback);
    }

    /** Configuración estándar: fuente por defecto en mapa de bits y el resto con AWT. */
    public static FontRoutingRasterizer standard() {
        return new FontRoutingRasterizer(Map.of(FontName.DEFAULT, new BitmapGlyphRasterizer()),
                new AwtGlyphRasterizer());
    }

    @Override
    public TextMask render(String text, FontSpec spec) {
        return routes.getOrDefault(spec.font(), fallback).render(text, spec);
    }
}
