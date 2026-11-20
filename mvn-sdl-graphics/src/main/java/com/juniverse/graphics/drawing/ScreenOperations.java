package com.juniverse.graphics.drawing;

/**
 * Screen and viewport clearing operations.
 */
public interface ScreenOperations {

    /** Clears the whole active page with the background color and moves the position to (0,0). */
    void clearDevice();

    /** Clears the current viewport with the background color and moves the position to (0,0). */
    void clearViewport();
}
