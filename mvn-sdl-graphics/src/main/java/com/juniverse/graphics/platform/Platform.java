package com.juniverse.graphics.platform;

import com.juniverse.graphics.dto.DisplaySpec;
import com.juniverse.graphics.dto.ScreenSize;
import com.juniverse.graphics.dto.InputEvent;

import java.util.function.Consumer;

/**
 * Servicios que la biblioteca necesita del sistema de ventanas.
 * <p>
 * Es el puerto que aísla al núcleo de cualquier tecnología concreta (SDL3 en
 * la implementación por defecto). Para pruebas o para otro backend basta con
 * proporcionar otra implementación.
 */
public interface Platform extends AutoCloseable {

    /** Crea y muestra una ventana nativa. */
    Display openDisplay(DisplaySpec spec);

    /** Entrega al consumidor todos los eventos pendientes sin bloquear. */
    void pollEvents(Consumer<InputEvent> consumer);

    /** Tamaño de la pantalla principal. */
    ScreenSize screenSize();

    /** Muestra un cuadro de diálogo de error. */
    void showErrorMessage(String title, String message);

    /** Libera los recursos globales de la plataforma. */
    @Override
    void close();
}
