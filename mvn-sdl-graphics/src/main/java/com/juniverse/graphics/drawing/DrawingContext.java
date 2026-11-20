package com.juniverse.graphics.drawing;

import com.juniverse.graphics.raster.Canvas;

import java.util.Objects;

/**
 * Dependencies shared by all drawing operations: the graphics
 * state, the active canvas and the change observer.
 * <p>
 * It is not a DTO: it groups collaborators with behavior, which is why it is a class
 * and not a record of the {@code dto} package.
 */
public final class DrawingContext {

    private final DrawingState state;
    private final Canvas canvas;
    private final SurfaceObserver observer;

    /**
     * @param state    graphics state of the window
     * @param canvas   canvas relative to the viewport of the active page
     * @param observer receiver of change notifications
     */
    public DrawingContext(DrawingState state, Canvas canvas, SurfaceObserver observer) {
        this.state = Objects.requireNonNull(state);
        this.canvas = Objects.requireNonNull(canvas);
        this.observer = Objects.requireNonNull(observer);
    }

    public DrawingState state() {
        return state;
    }

    public Canvas canvas() {
        return canvas;
    }

    public SurfaceObserver observer() {
        return observer;
    }

    /** Notifies that the active page changed. */
    public void changed() {
        observer.surfaceChanged();
    }
}
