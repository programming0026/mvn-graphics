package com.juniverse.graphics.exception;

/**
 * Se lanza cuando se intenta dibujar sin una ventana gráfica abierta.
 */
public class NoCurrentWindowException extends IllegalStateException {

    public NoCurrentWindowException() {
        super("A graphics operation was attempted without a current window. Call initWindow or initGraph first.");
    }
}
