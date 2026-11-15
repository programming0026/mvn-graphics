package com.juniverse.graphics.drawing;

import com.juniverse.graphics.dto.Point;
import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.raster.FrameBuffer;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Implementación de {@link ScreenOperations}.
 * <p>
 * A diferencia de los demás dibujantes, necesita acceso directo a la página
 * activa porque {@code cleardevice} ignora el viewport.
 */
public final class ScreenClearer implements ScreenOperations {

    private final DrawingContext context;
    private final Supplier<FrameBuffer> activePage;

    public ScreenClearer(DrawingContext context, Supplier<FrameBuffer> activePage) {
        this.context = Objects.requireNonNull(context);
        this.activePage = Objects.requireNonNull(activePage);
    }

    @Override
    public void clearDevice() {
        DrawingState state = context.state();
        activePage.get().fill(state.backgroundArgb());
        state.setPosition(new Point(0, 0));
        context.changed();
    }

    @Override
    public void clearViewport() {
        DrawingState state = context.state();
        Viewport vp = state.viewport();
        FrameBuffer page = activePage.get();
        int background = state.backgroundArgb();
        for (int y = Math.max(0, vp.top()); y <= Math.min(page.height() - 1, vp.bottom()); y++) {
            for (int x = Math.max(0, vp.left()); x <= Math.min(page.width() - 1, vp.right()); x++) {
                page.set(x, y, background);
            }
        }
        state.setPosition(new Point(0, 0));
        context.changed();
    }
}
