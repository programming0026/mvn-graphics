package com.juniverse.graphics.window;

/**
 * Política que decide cuándo procesar eventos y refrescar la pantalla
 * después de una operación de dibujo.
 */
@FunctionalInterface
public interface FrameScheduler {

    /** Planificador que nunca refresca automáticamente (útil en pruebas). */
    FrameScheduler MANUAL = () -> { };

    /** Solicita un refresco; la implementación puede posponerlo para limitar los FPS. */
    void requestFrame();
}
