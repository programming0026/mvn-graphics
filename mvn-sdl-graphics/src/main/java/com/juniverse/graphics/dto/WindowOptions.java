package com.juniverse.graphics.dto;

/**
 * Opciones de comportamiento de una ventana gráfica.
 *
 * @param doubleBuffered si es verdadero se dibuja en la página 1 mientras se
 *                       muestra la página 0 (use {@code swapBuffers} para alternar)
 * @param exitOnClose    si es verdadero, cerrar la ventana termina el programa
 */
public record WindowOptions(boolean doubleBuffered, boolean exitOnClose) {

    /** Opciones por defecto de {@code initwindow}: sin doble búfer y salir al cerrar. */
    public static final WindowOptions DEFAULT = new WindowOptions(false, true);
}
