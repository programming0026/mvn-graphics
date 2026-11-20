package com.juniverse.graphics.platform.sdl;

import com.juniverse.graphics.dto.InputEvent;
import com.juniverse.graphics.platform.Display;
import com.juniverse.graphics.dto.DisplaySpec;
import com.juniverse.graphics.platform.Platform;
import com.juniverse.graphics.exception.PlatformException;
import com.juniverse.graphics.dto.ScreenSize;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_Rect;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.lwjgl.sdl.SDLError.SDL_GetError;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_MOUSE_MOTION;
import static org.lwjgl.sdl.SDLEvents.SDL_PollEvent;
import static org.lwjgl.sdl.SDLInit.SDL_INIT_VIDEO;
import static org.lwjgl.sdl.SDLInit.SDL_Init;
import static org.lwjgl.sdl.SDLInit.SDL_Quit;
import static org.lwjgl.sdl.SDLMessageBox.SDL_MESSAGEBOX_ERROR;
import static org.lwjgl.sdl.SDLMessageBox.SDL_ShowSimpleMessageBox;
import static org.lwjgl.sdl.SDLVideo.SDL_GetDisplayBounds;
import static org.lwjgl.sdl.SDLVideo.SDL_GetPrimaryDisplay;

/**
 * Implementation of {@link Platform} with SDL3 (through LWJGL).
 * <p>
 * It must always be created and used from the same thread (on macOS, the main
 * thread, running the JVM with {@code -XstartOnFirstThread}).
 */
public final class SdlPlatform implements Platform {

    private final SDL_Event event = SDL_Event.malloc();
    private final Map<Integer, SdlDisplay> displays = new HashMap<>();
    private boolean closed;

    /**
     * Initializes the SDL video subsystem.
     *
     * @throws PlatformException if SDL cannot be initialized
     */
    public SdlPlatform() {
        if (!SDL_Init(SDL_INIT_VIDEO)) {
            event.free();
            throw new PlatformException("Could not initialize SDL3: " + SDL_GetError());
        }
    }

    @Override
    public Display openDisplay(DisplaySpec spec) {
        ensureOpen();
        SdlDisplay display = new SdlDisplay(spec, disposed -> displays.remove(disposed.id()));
        displays.put(display.id(), display);
        return display;
    }

    @Override
    public void pollEvents(Consumer<InputEvent> consumer) {
        ensureOpen();
        while (SDL_PollEvent(event)) {
            convertMouseCoordinates();
            SdlEventTranslator.translate(event, consumer);
        }
    }

    @Override
    public ScreenSize screenSize() {
        ensureOpen();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_Rect bounds = SDL_Rect.malloc(stack);
            if (!SDL_GetDisplayBounds(SDL_GetPrimaryDisplay(), bounds)) {
                throw new PlatformException("Could not get the screen size: " + SDL_GetError());
            }
            return new ScreenSize(bounds.w(), bounds.h());
        }
    }

    @Override
    public void showErrorMessage(String title, String message) {
        SDL_ShowSimpleMessageBox(SDL_MESSAGEBOX_ERROR, title, message, 0L);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        event.free();
        SDL_Quit();
    }

    /**
     * Windows may be scaled: mouse coordinates are
     * converted from window pixels to BGI coordinates.
     */
    private void convertMouseCoordinates() {
        int windowId = switch (event.type()) {
            case SDL_EVENT_MOUSE_MOTION -> event.motion().windowID();
            case SDL_EVENT_MOUSE_BUTTON_DOWN, SDL_EVENT_MOUSE_BUTTON_UP -> event.button().windowID();
            default -> 0;
        };
        SdlDisplay display = displays.get(windowId);
        if (display != null) {
            display.toLogicalCoordinates(event);
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new PlatformException("The SDL platform has already been closed");
        }
    }
}
