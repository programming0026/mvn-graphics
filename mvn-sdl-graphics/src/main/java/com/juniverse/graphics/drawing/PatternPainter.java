package com.juniverse.graphics.drawing;

import com.juniverse.graphics.raster.SpanSink;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.style.PatternMask;
import com.juniverse.graphics.style.RasterOperation;

/**
 * Paints horizontal spans using the current fill pattern and color.
 * <p>
 * The set bits of the pattern use the fill color and the unset ones the
 * background color, same as in BGI.
 */
public final class PatternPainter implements SpanSink {

    private final DrawingContext context;

    public PatternPainter(DrawingContext context) {
        this.context = context;
    }

    @Override
    public void accept(int y, int xStart, int xEnd) {
        DrawingState state = context.state();
        int background = state.backgroundArgb();
        boolean empty = state.fillSettings().pattern() == FillPattern.EMPTY;
        int foreground = empty ? background : state.palette().toArgb(state.fillSettings().color());
        PatternMask mask = state.activeFillMask();
        for (int x = xStart; x <= xEnd; x++) {
            int argb = mask.isSet(x, y) ? foreground : background;
            context.canvas().plot(x, y, argb, RasterOperation.COPY);
        }
    }
}
