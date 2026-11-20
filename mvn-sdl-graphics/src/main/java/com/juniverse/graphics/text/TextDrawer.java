package com.juniverse.graphics.text;

import com.juniverse.graphics.dto.CharScale;
import com.juniverse.graphics.dto.TextSettings;
import com.juniverse.graphics.drawing.DrawingContext;
import com.juniverse.graphics.dto.Point;
import com.juniverse.graphics.style.RasterOperation;

import java.util.Objects;

/**
 * Implementation of {@link TextOperations}.
 * <p>
 * It depends on the {@link GlyphRasterizer} abstraction to obtain the glyphs,
 * so the font engine can be freely replaced.
 */
public final class TextDrawer implements TextOperations {

    private final DrawingContext context;
    private final TextState textState;
    private final GlyphRasterizer rasterizer;

    public TextDrawer(DrawingContext context, TextState textState, GlyphRasterizer rasterizer) {
        this.context = Objects.requireNonNull(context);
        this.textState = Objects.requireNonNull(textState);
        this.rasterizer = Objects.requireNonNull(rasterizer);
    }

    @Override
    public void outText(String text) {
        Point position = context.state().position();
        outTextXY(position.x(), position.y(), text);
        TextSettings settings = textState.settings();
        if (settings.direction() == TextDirection.HORIZONTAL && settings.horizontal() == HorizontalAlignment.LEFT) {
            context.state().setPosition(position.translate(textWidth(text), 0));
        }
    }

    @Override
    public void outTextXY(int x, int y, String text) {
        TextSettings settings = textState.settings();
        TextMask mask = rasterizer.render(text, textState.fontSpec());
        if (settings.direction() == TextDirection.VERTICAL) {
            mask = mask.rotateCounterClockwise();
        }
        int originX = x + settings.horizontal().offset(mask.width());
        int originY = y + settings.vertical().offset(mask.height());
        int argb = context.state().colorArgb();
        for (int row = 0; row < mask.height(); row++) {
            for (int column = 0; column < mask.width(); column++) {
                if (mask.isSet(column, row)) {
                    context.canvas().plot(originX + column, originY + row, argb, RasterOperation.COPY);
                }
            }
        }
        context.changed();
    }

    @Override
    public int textWidth(String text) {
        return rasterizer.render(text, textState.fontSpec()).width();
    }

    @Override
    public int textHeight(String text) {
        return rasterizer.render(text, textState.fontSpec()).height();
    }

    @Override
    public void setTextStyle(FontName font, TextDirection direction, int charSize) {
        textState.setSettings(textState.settings().withStyle(font, direction, charSize));
    }

    @Override
    public void setTextJustify(HorizontalAlignment horizontal, VerticalAlignment vertical) {
        textState.setSettings(textState.settings().withJustification(horizontal, vertical));
    }

    @Override
    public void setUserCharSize(int multX, int divX, int multY, int divY) {
        textState.setCharScale(new CharScale(multX, divX, multY, divY));
    }

    @Override
    public TextSettings textSettings() {
        return textState.settings();
    }
}
