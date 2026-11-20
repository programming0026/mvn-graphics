package com.juniverse.graphics.drawing;

/**
 * Observer that receives a notification every time the active page
 * is modified. The window uses it to decide when to refresh the screen.
 */
@FunctionalInterface
public interface SurfaceObserver {

    /** Observer that ignores notifications (useful in tests). */
    SurfaceObserver NONE = () -> { };

    void surfaceChanged();
}
