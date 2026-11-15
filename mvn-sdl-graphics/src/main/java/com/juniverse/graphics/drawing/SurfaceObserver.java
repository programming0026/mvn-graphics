package com.juniverse.graphics.drawing;

/**
 * Observador que recibe una notificación cada vez que se modifica la página
 * activa. La ventana lo usa para decidir cuándo refrescar la pantalla.
 */
@FunctionalInterface
public interface SurfaceObserver {

    /** Observador que ignora las notificaciones (útil en pruebas). */
    SurfaceObserver NONE = () -> { };

    void surfaceChanged();
}
