package com.juniverse.graphics.examples.exception;

/**
 * Indicates that an example ended early: the user pressed ESC or
 * the graphics mode could not be started.
 * <p>
 * Replaces the {@code exit()} calls of the original Turbo C programs. This way
 * each example ends cleanly and the {@link Main} menu regains
 * control, instead of closing the whole application with {@code System.exit}.
 */
public class ExampleAbortedException extends RuntimeException {

    public ExampleAbortedException(String message) {
        super(message);
    }
}
