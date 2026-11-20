package com.juniverse.graphics.dto;

import com.juniverse.graphics.input.ExtendedKey;
import com.juniverse.graphics.input.MouseButton;

/**
 * Platform-independent input events.
 * <p>
 * The platform layer (SDL) translates its native events to these types, so
 * that the rest of the library never depends on SDL directly.
 * {@code displayId} identifies the native window that originated the event.
 */
public sealed interface InputEvent {

    /** Character or control code typed (Enter = 13, Esc = 27, ...). */
    public record KeyTyped(int displayId, int code) implements InputEvent {
    }

    /** Extended key pressed (arrows, F1..F9, Home, ...). */
    public record ExtendedKeyPressed(int displayId, ExtendedKey key) implements InputEvent {
    }

    /** The mouse moved inside the window. */
    public record MouseMoved(int displayId, int x, int y) implements InputEvent {
    }

    /** A mouse button changed state. {@code clicks} is 2 on a double click. */
    public record MouseButtonChanged(int displayId, MouseButton button, boolean pressed, int clicks,
                              int x, int y) implements InputEvent {
    }

    /** The user asked to close the window. */
    public record CloseRequested(int displayId) implements InputEvent {
    }

    /** The window changed size (for example, when maximized). */
    public record Resized(int displayId) implements InputEvent {
    }

    /** The window needs to be redrawn. */
    public record Exposed(int displayId) implements InputEvent {
    }

    /** Application termination was requested (for example, with Ctrl+C). */
    public record QuitRequested() implements InputEvent {
    }
}
