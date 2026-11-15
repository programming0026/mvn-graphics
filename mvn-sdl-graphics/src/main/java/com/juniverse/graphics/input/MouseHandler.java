package com.juniverse.graphics.input;

/**
 * Manejador de eventos de mouse (equivale a {@code registermousehandler}).
 * Recibe las coordenadas del evento en la ventana.
 */
@FunctionalInterface
public interface MouseHandler {

    void handle(int x, int y);
}
