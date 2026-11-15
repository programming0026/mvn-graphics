package com.juniverse.graphics.drawing;

/**
 * Operaciones de borrado de pantalla y viewport.
 */
public interface ScreenOperations {

    /** Borra toda la página activa con el color de fondo y mueve la posición a (0,0). */
    void clearDevice();

    /** Borra el viewport actual con el color de fondo y mueve la posición a (0,0). */
    void clearViewport();
}
