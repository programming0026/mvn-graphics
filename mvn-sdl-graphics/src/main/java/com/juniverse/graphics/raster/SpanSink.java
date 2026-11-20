package com.juniverse.graphics.raster;

/**
 * Receiver of horizontal spans generated when filling shapes.
 */
@FunctionalInterface
public interface SpanSink {

    /**
     * Receives an inclusive horizontal span.
     *
     * @param y      row of the span
     * @param xStart first column (inclusive)
     * @param xEnd   last column (inclusive), always &gt;= {@code xStart}
     */
    void accept(int y, int xStart, int xEnd);
}
