package com.juniverse.graphics.drawing;

/**
 * Operaciones de líneas y de la posición actual (CP).
 */
public interface LineOperations {

    /** Dibuja una línea entre dos puntos (no modifica la posición actual). */
    void line(int x1, int y1, int x2, int y2);

    /** Dibuja una línea desde la posición actual hasta (x, y) y mueve la posición. */
    void lineTo(int x, int y);

    /** Dibuja una línea relativa a la posición actual y mueve la posición. */
    void lineRel(int dx, int dy);

    /** Mueve la posición actual a (x, y). */
    void moveTo(int x, int y);

    /** Mueve la posición actual de forma relativa. */
    void moveRel(int dx, int dy);

    /** Coordenada x de la posición actual (relativa al viewport). */
    int getX();

    /** Coordenada y de la posición actual (relativa al viewport). */
    int getY();
}
