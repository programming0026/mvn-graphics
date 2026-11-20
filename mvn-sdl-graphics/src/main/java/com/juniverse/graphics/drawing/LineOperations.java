package com.juniverse.graphics.drawing;

/**
 * Line and current position (CP) operations.
 */
public interface LineOperations {

    /** Draws a line between two points (does not modify the current position). */
    void line(int x1, int y1, int x2, int y2);

    /** Draws a line from the current position to (x, y) and moves the position. */
    void lineTo(int x, int y);

    /** Draws a line relative to the current position and moves the position. */
    void lineRel(int dx, int dy);

    /** Moves the current position to (x, y). */
    void moveTo(int x, int y);

    /** Moves the current position relatively. */
    void moveRel(int dx, int dy);

    /** x coordinate of the current position (relative to the viewport). */
    int getX();

    /** y coordinate of the current position (relative to the viewport). */
    int getY();
}
