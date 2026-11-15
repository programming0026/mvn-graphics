package com.juniverse.graphics.drawing;

import com.juniverse.graphics.raster.Canvas;

import java.util.Objects;

/**
 * Dependencias compartidas por todas las operaciones de dibujo: el estado
 * gráfico, el lienzo activo y el observador de cambios.
 * <p>
 * No es un DTO: agrupa colaboradores con comportamiento, por eso es una clase
 * y no un record del paquete {@code dto}.
 */
public final class DrawingContext {

    private final DrawingState state;
    private final Canvas canvas;
    private final SurfaceObserver observer;

    /**
     * @param state    estado gráfico de la ventana
     * @param canvas   lienzo relativo al viewport de la página activa
     * @param observer receptor de notificaciones de cambio
     */
    public DrawingContext(DrawingState state, Canvas canvas, SurfaceObserver observer) {
        this.state = Objects.requireNonNull(state);
        this.canvas = Objects.requireNonNull(canvas);
        this.observer = Objects.requireNonNull(observer);
    }

    public DrawingState state() {
        return state;
    }

    public Canvas canvas() {
        return canvas;
    }

    public SurfaceObserver observer() {
        return observer;
    }

    /** Notifica que la página activa cambió. */
    public void changed() {
        observer.surfaceChanged();
    }
}
