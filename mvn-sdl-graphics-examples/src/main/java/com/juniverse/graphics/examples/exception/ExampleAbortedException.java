package com.juniverse.graphics.examples.exception;

/**
 * Indica que un ejemplo terminó antes de tiempo: el usuario presionó ESC o
 * no se pudo iniciar el modo gráfico.
 * <p>
 * Reemplaza los {@code exit()} de los programas originales de Turbo C. Así
 * cada ejemplo termina limpiamente y el menú de {@link Main} recupera el
 * control, en lugar de cerrar toda la aplicación con {@code System.exit}.
 */
public class ExampleAbortedException extends RuntimeException {

    public ExampleAbortedException(String message) {
        super(message);
    }
}
