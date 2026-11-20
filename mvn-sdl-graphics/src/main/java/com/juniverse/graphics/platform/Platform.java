package com.juniverse.graphics.platform;

import com.juniverse.graphics.dto.DisplaySpec;
import com.juniverse.graphics.dto.ScreenSize;
import com.juniverse.graphics.dto.InputEvent;

import java.util.function.Consumer;

/**
 * Services the library needs from the window system.
 * <p>
 * It is the port that isolates the core from any concrete technology (SDL3 in
 * the default implementation). For tests or another backend it is enough to
 * provide another implementation.
 */
public interface Platform extends AutoCloseable {

    /** Creates and shows a native window. */
    Display openDisplay(DisplaySpec spec);

    /** Delivers all pending events to the consumer without blocking. */
    void pollEvents(Consumer<InputEvent> consumer);

    /** Size of the main screen. */
    ScreenSize screenSize();

    /** Shows an error dialog. */
    void showErrorMessage(String title, String message);

    /** Releases the global resources of the platform. */
    @Override
    void close();
}
