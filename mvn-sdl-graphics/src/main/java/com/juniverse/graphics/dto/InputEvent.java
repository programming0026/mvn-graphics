package com.juniverse.graphics.dto;

import com.juniverse.graphics.input.ExtendedKey;
import com.juniverse.graphics.input.MouseButton;

/**
 * Eventos de entrada independientes de la plataforma.
 * <p>
 * La capa de plataforma (SDL) traduce sus eventos nativos a estos tipos, de
 * modo que el resto de la biblioteca nunca depende de SDL directamente.
 * {@code displayId} identifica la ventana nativa que originó el evento.
 */
public sealed interface InputEvent {

    /** Carácter o código de control escrito (Enter = 13, Esc = 27, ...). */
    public record KeyTyped(int displayId, int code) implements InputEvent {
    }

    /** Tecla extendida presionada (flechas, F1..F9, Inicio, ...). */
    public record ExtendedKeyPressed(int displayId, ExtendedKey key) implements InputEvent {
    }

    /** El mouse se movió dentro de la ventana. */
    public record MouseMoved(int displayId, int x, int y) implements InputEvent {
    }

    /** Un botón del mouse cambió de estado. {@code clicks} vale 2 en un doble clic. */
    public record MouseButtonChanged(int displayId, MouseButton button, boolean pressed, int clicks,
                              int x, int y) implements InputEvent {
    }

    /** El usuario pidió cerrar la ventana. */
    public record CloseRequested(int displayId) implements InputEvent {
    }

    /** La ventana cambió de tamaño (por ejemplo, al maximizarla). */
    public record Resized(int displayId) implements InputEvent {
    }

    /** La ventana necesita volver a dibujarse. */
    public record Exposed(int displayId) implements InputEvent {
    }

    /** Se solicitó terminar la aplicación (por ejemplo, con Ctrl+C). */
    public record QuitRequested() implements InputEvent {
    }
}
