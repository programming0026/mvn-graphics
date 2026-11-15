package com.juniverse.graphics.raster;

/**
 * Receptor de tramos horizontales (spans) generados al rellenar figuras.
 */
@FunctionalInterface
public interface SpanSink {

    /**
     * Recibe un tramo horizontal inclusivo.
     *
     * @param y      fila del tramo
     * @param xStart primera columna (inclusive)
     * @param xEnd   última columna (inclusive), siempre &gt;= {@code xStart}
     */
    void accept(int y, int xStart, int xEnd);
}
