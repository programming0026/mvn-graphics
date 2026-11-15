package com.juniverse.graphics.drawing;

import com.juniverse.graphics.raster.SpanSink;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.style.PatternMask;
import com.juniverse.graphics.style.RasterOperation;

/**
 * Pinta tramos horizontales usando el patrón y el color de relleno actuales.
 * <p>
 * Los bits activos del patrón usan el color de relleno y los inactivos el
 * color de fondo, igual que en BGI.
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
