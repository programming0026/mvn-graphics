package com.juniverse.graphics.text;

import com.juniverse.graphics.dto.FontSpec;
import com.juniverse.graphics.dto.GlyphCacheKey;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Text rasterizer based on Java fonts (AWT in headless mode).
 * <p>
 * The text is drawn without anti-aliasing on a grayscale image and
 * converted into a {@link TextMask}. Results are stored in an LRU
 * cache because BGI programs tend to repeat the same texts.
 */
public final class AwtGlyphRasterizer implements GlyphRasterizer {

    private static final int CACHE_SIZE = 256;
    private static final int THRESHOLD = 128;
    /** Character shown in place of those the font cannot draw. */
    private static final int MISSING_GLYPH = '?';

    static {
        if (System.getProperty("java.awt.headless") == null) {
            System.setProperty("java.awt.headless", "true");
        }
    }

    private final Map<GlyphCacheKey, TextMask> cache = new LinkedHashMap<>(CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<GlyphCacheKey, TextMask> eldest) {
            return size() > CACHE_SIZE;
        }
    };

    @Override
    public TextMask render(String text, FontSpec spec) {
        return cache.computeIfAbsent(new GlyphCacheKey(text, spec), key -> rasterize(key.text(), key.spec()));
    }

    private static TextMask rasterize(String requestedText, FontSpec spec) {
        Font font = toAwtFont(spec);
        String text = displayableText(requestedText, font);
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D probeGraphics = probe.createGraphics();
        FontMetrics metrics = probeGraphics.getFontMetrics(font);
        FontRenderContext renderContext = probeGraphics.getFontRenderContext();
        int ascent = metrics.getAscent();
        int height = Math.max(1, ascent + metrics.getDescent());
        int width = (int) Math.ceil(font.getStringBounds(text, renderContext).getWidth());
        probeGraphics.dispose();

        if (text.isEmpty() || width <= 0) {
            return TextMask.empty(height);
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setFont(font);
        graphics.setColor(java.awt.Color.WHITE);
        graphics.drawString(text, 0, ascent);
        graphics.dispose();

        Raster raster = image.getRaster();
        boolean[] bits = new boolean[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bits[y * width + x] = raster.getSample(x, y, 0) >= THRESHOLD;
            }
        }
        return new TextMask(width, height, bits);
    }

    /**
     * Replaces with {@link #MISSING_GLYPH} the characters that cannot be
     * drawn: control characters (for example, U+0080..U+009F, which is what you
     * get when converting DOS codes 128..159 to {@code char}) and those
     * the font does not have. Without this, AWT draws an empty box for each one.
     * It is the same criterion used by {@link BitmapFont}.
     */
    private static String displayableText(String text, Font font) {
        StringBuilder displayable = new StringBuilder(text.length());
        text.codePoints().forEach(codePoint -> displayable.appendCodePoint(
                Character.isISOControl(codePoint) || !font.canDisplay(codePoint) ? MISSING_GLYPH : codePoint));
        return displayable.toString();
    }

    private static Font toAwtFont(FontSpec spec) {
        Font font = new Font(spec.font().family(), spec.font().style(), 1).deriveFont(spec.pixelSize());
        if (spec.stretchX() != 1.0) {
            font = font.deriveFont(AffineTransform.getScaleInstance(spec.stretchX(), 1.0));
        }
        return font;
    }

}
