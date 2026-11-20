package com.juniverse.graphics.exception;

/**
 * Error produced by the platform layer (for example, if SDL cannot be
 * initialized or create a window).
 */
public class PlatformException extends RuntimeException {

    public PlatformException(String message) {
        super(message);
    }
}
