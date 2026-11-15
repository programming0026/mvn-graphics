package com.juniverse.graphics.exception;

/**
 * Error producido por la capa de plataforma (por ejemplo, si SDL no puede
 * inicializarse o crear una ventana).
 */
public class PlatformException extends RuntimeException {

    public PlatformException(String message) {
        super(message);
    }
}
