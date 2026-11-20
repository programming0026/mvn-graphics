package com.juniverse.graphics.input;

/**
 * Mouse event handler (equivalent to {@code registermousehandler}).
 * Receives the coordinates of the event in the window.
 */
@FunctionalInterface
public interface MouseHandler {

    void handle(int x, int y);
}
