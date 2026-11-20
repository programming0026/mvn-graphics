package com.juniverse.graphics.exception;

/**
 * Thrown when trying to draw without an open graphics window.
 */
public class NoCurrentWindowException extends IllegalStateException {

    public NoCurrentWindowException() {
        super("A graphics operation was attempted without a current window. Call initWindow or initGraph first.");
    }
}
