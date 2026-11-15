package com.juniverse.graphics.platform.sdl;

import com.juniverse.graphics.input.ExtendedKey;
import com.juniverse.graphics.dto.InputEvent;

import java.util.Map;
import java.util.Optional;

import static org.lwjgl.sdl.SDLKeycode.SDLK_BACKSPACE;
import static org.lwjgl.sdl.SDLKeycode.SDLK_DELETE;
import static org.lwjgl.sdl.SDLKeycode.SDLK_DOWN;
import static org.lwjgl.sdl.SDLKeycode.SDLK_END;
import static org.lwjgl.sdl.SDLKeycode.SDLK_ESCAPE;
import static org.lwjgl.sdl.SDLKeycode.SDLK_F1;
import static org.lwjgl.sdl.SDLKeycode.SDLK_HOME;
import static org.lwjgl.sdl.SDLKeycode.SDLK_INSERT;
import static org.lwjgl.sdl.SDLKeycode.SDLK_KP_ENTER;
import static org.lwjgl.sdl.SDLKeycode.SDLK_LEFT;
import static org.lwjgl.sdl.SDLKeycode.SDLK_PAGEDOWN;
import static org.lwjgl.sdl.SDLKeycode.SDLK_PAGEUP;
import static org.lwjgl.sdl.SDLKeycode.SDLK_RETURN;
import static org.lwjgl.sdl.SDLKeycode.SDLK_RIGHT;
import static org.lwjgl.sdl.SDLKeycode.SDLK_TAB;
import static org.lwjgl.sdl.SDLKeycode.SDLK_UP;
import static org.lwjgl.sdl.SDLKeycode.SDL_KMOD_CTRL;

/**
 * Traduce las teclas de SDL a los códigos que espera {@code getch()}.
 * <p>
 * Los caracteres imprimibles llegan por {@code SDL_EVENT_TEXT_INPUT}; aquí se
 * resuelven solo las teclas de control y las extendidas.
 */
public final class SdlKeyTranslator {

    private static final int FUNCTION_KEYS = 9;
    private static final int CONTROL_OFFSET = 'a' - 1;

    private static final Map<Integer, Integer> CONTROL_KEYS = Map.of(
            SDLK_RETURN, 13,
            SDLK_KP_ENTER, 13,
            SDLK_ESCAPE, 27,
            SDLK_BACKSPACE, 8,
            SDLK_TAB, 9);

    private static final Map<Integer, ExtendedKey> EXTENDED_KEYS = Map.ofEntries(
            Map.entry(SDLK_HOME, ExtendedKey.HOME),
            Map.entry(SDLK_UP, ExtendedKey.UP),
            Map.entry(SDLK_PAGEUP, ExtendedKey.PAGE_UP),
            Map.entry(SDLK_LEFT, ExtendedKey.LEFT),
            Map.entry(SDLK_RIGHT, ExtendedKey.RIGHT),
            Map.entry(SDLK_END, ExtendedKey.END),
            Map.entry(SDLK_DOWN, ExtendedKey.DOWN),
            Map.entry(SDLK_PAGEDOWN, ExtendedKey.PAGE_DOWN),
            Map.entry(SDLK_INSERT, ExtendedKey.INSERT),
            Map.entry(SDLK_DELETE, ExtendedKey.DELETE));

    private SdlKeyTranslator() {
    }

    /**
     * Traduce una pulsación de tecla.
     *
     * @return el evento equivalente, o vacío si la tecla llegará como texto
     */
    public static Optional<InputEvent> translate(int displayId, int keycode, int modifiers) {
        Integer control = CONTROL_KEYS.get(keycode);
        if (control != null) {
            return Optional.of(new InputEvent.KeyTyped(displayId, control));
        }
        ExtendedKey extended = EXTENDED_KEYS.get(keycode);
        if (extended != null) {
            return Optional.of(new InputEvent.ExtendedKeyPressed(displayId, extended));
        }
        int functionIndex = keycode - SDLK_F1;
        if (functionIndex >= 0 && functionIndex < FUNCTION_KEYS) {
            ExtendedKey function = ExtendedKey.valueOf("F" + (functionIndex + 1));
            return Optional.of(new InputEvent.ExtendedKeyPressed(displayId, function));
        }
        if ((modifiers & SDL_KMOD_CTRL) != 0 && keycode >= 'a' && keycode <= 'z') {
            // Ctrl+A = 1 ... Ctrl+Z = 26, como en conio.
            return Optional.of(new InputEvent.KeyTyped(displayId, keycode - CONTROL_OFFSET));
        }
        return Optional.empty();
    }
}
