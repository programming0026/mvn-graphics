package com.juniverse.graphics.platform.sdl;

import com.juniverse.graphics.dto.InputEvent;
import com.juniverse.graphics.input.MouseButton;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_KeyboardEvent;
import org.lwjgl.sdl.SDL_MouseButtonEvent;
import org.lwjgl.sdl.SDL_MouseMotionEvent;

import java.util.Optional;
import java.util.function.Consumer;

import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_KEY_DOWN;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_MOUSE_MOTION;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_QUIT;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_TEXT_INPUT;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_WINDOW_EXPOSED;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_WINDOW_MAXIMIZED;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_WINDOW_RESIZED;
import static org.lwjgl.sdl.SDLEvents.SDL_EVENT_WINDOW_RESTORED;
import static org.lwjgl.sdl.SDLMouse.SDL_BUTTON_LEFT;
import static org.lwjgl.sdl.SDLMouse.SDL_BUTTON_MIDDLE;
import static org.lwjgl.sdl.SDLMouse.SDL_BUTTON_RIGHT;

/**
 * Convierte eventos nativos de SDL3 en {@link InputEvent} de la biblioteca.
 */
public final class SdlEventTranslator {

    private SdlEventTranslator() {
    }

    /** Traduce un evento SDL y entrega cero o más eventos equivalentes. */
    public static void translate(SDL_Event event, Consumer<InputEvent> consumer) {
        switch (event.type()) {
            case SDL_EVENT_QUIT -> consumer.accept(new InputEvent.QuitRequested());
            case SDL_EVENT_WINDOW_CLOSE_REQUESTED ->
                    consumer.accept(new InputEvent.CloseRequested(event.window().windowID()));
            case SDL_EVENT_WINDOW_EXPOSED -> consumer.accept(new InputEvent.Exposed(event.window().windowID()));
            case SDL_EVENT_WINDOW_RESIZED, SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED,
                 SDL_EVENT_WINDOW_MAXIMIZED, SDL_EVENT_WINDOW_RESTORED ->
                    consumer.accept(new InputEvent.Resized(event.window().windowID()));
            case SDL_EVENT_KEY_DOWN -> {
                SDL_KeyboardEvent key = event.key();
                SdlKeyTranslator.translate(key.windowID(), key.key(), key.mod()).ifPresent(consumer);
            }
            case SDL_EVENT_TEXT_INPUT -> {
                int displayId = event.text().windowID();
                String text = event.text().textString();
                if (text != null) {
                    text.chars().forEach(ch -> consumer.accept(new InputEvent.KeyTyped(displayId, ch)));
                }
            }
            case SDL_EVENT_MOUSE_MOTION -> {
                SDL_MouseMotionEvent motion = event.motion();
                consumer.accept(new InputEvent.MouseMoved(motion.windowID(),
                        (int) motion.x(), (int) motion.y()));
            }
            case SDL_EVENT_MOUSE_BUTTON_DOWN, SDL_EVENT_MOUSE_BUTTON_UP -> {
                SDL_MouseButtonEvent button = event.button();
                toButton(button.button()).ifPresent(mouseButton -> consumer.accept(
                        new InputEvent.MouseButtonChanged(button.windowID(), mouseButton, button.down(),
                                Byte.toUnsignedInt(button.clicks()), (int) button.x(), (int) button.y())));
            }
            default -> {
                // Los demás eventos de SDL no son relevantes para BGI.
            }
        }
    }

    private static Optional<MouseButton> toButton(byte sdlButton) {
        return switch (sdlButton) {
            case SDL_BUTTON_LEFT -> Optional.of(MouseButton.LEFT);
            case SDL_BUTTON_RIGHT -> Optional.of(MouseButton.RIGHT);
            case SDL_BUTTON_MIDDLE -> Optional.of(MouseButton.MIDDLE);
            default -> Optional.empty();
        };
    }
}
