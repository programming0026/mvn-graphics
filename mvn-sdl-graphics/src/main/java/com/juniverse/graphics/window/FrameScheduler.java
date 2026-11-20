package com.juniverse.graphics.window;

/**
 * Policy that decides when to process events and refresh the screen
 * after a drawing operation.
 */
@FunctionalInterface
public interface FrameScheduler {

    /** Scheduler that never refreshes automatically (useful in tests). */
    FrameScheduler MANUAL = () -> { };

    /** Requests a refresh; the implementation may postpone it to limit the FPS. */
    void requestFrame();
}
