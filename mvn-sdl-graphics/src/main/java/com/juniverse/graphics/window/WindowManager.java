package com.juniverse.graphics.window;

import com.juniverse.graphics.dto.WindowOptions;
import com.juniverse.graphics.exception.NoCurrentWindowException;
import com.juniverse.graphics.driver.GraphicsDriver;
import com.juniverse.graphics.dto.GraphicsMode;
import com.juniverse.graphics.image.ImageFileStore;
import com.juniverse.graphics.dto.InputEvent;
import com.juniverse.graphics.platform.Display;
import com.juniverse.graphics.dto.DisplaySpec;
import com.juniverse.graphics.platform.Platform;
import com.juniverse.graphics.dto.ScreenSize;
import com.juniverse.graphics.text.GlyphRasterizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Administra las ventanas gráficas abiertas, la ventana actual y el ciclo de
 * eventos.
 * <p>
 * BGI es una API de modo inmediato y bloqueante ({@code getch}, {@code delay}),
 * así que no hay un hilo de interfaz separado: los eventos se procesan en el
 * hilo del programa cada vez que este espera o, como máximo una vez por
 * cuadro (~60 FPS), mientras dibuja. Así SDL siempre se usa desde un único hilo.
 */
public final class WindowManager implements FrameScheduler {

    /** Intervalo mínimo entre refrescos automáticos (~60 cuadros por segundo). */
    private static final long FRAME_INTERVAL_NANOS = 1_000_000_000L / 60;

    /** Pausa máxima entre consultas de eventos al esperar. */
    private static final long POLL_SLEEP_MILLIS = 5;

    /** Identificador que indica que no hay ventana actual. */
    public static final int NO_CURRENT_WINDOW = -1;

    private final Supplier<Platform> platformFactory;
    private final GlyphRasterizer glyphs;
    private final ImageFileStore fileStore;
    private final Runnable exitAction;
    private final LongSupplier clock;

    private final List<GraphicsWindow> windows = new ArrayList<>();
    private Platform platform;
    private int currentId = NO_CURRENT_WINDOW;
    private boolean processing;
    private boolean exitRequested;
    private GraphicsError pendingError = GraphicsError.OK;
    private long lastFrameNanos;

    /**
     * @param platformFactory crea la plataforma la primera vez que se abre una ventana
     * @param glyphs          motor de texto compartido por las ventanas
     * @param fileStore       almacenamiento de imágenes compartido por las ventanas
     * @param exitAction      acción ejecutada cuando el usuario cierra una ventana con
     *                        {@link WindowOptions#exitOnClose()} (normalmente {@code System.exit})
     * @param clock           reloj monotónico en nanosegundos
     */
    public WindowManager(Supplier<Platform> platformFactory, GlyphRasterizer glyphs, ImageFileStore fileStore,
                         Runnable exitAction, LongSupplier clock) {
        this.platformFactory = Objects.requireNonNull(platformFactory);
        this.glyphs = Objects.requireNonNull(glyphs);
        this.fileStore = Objects.requireNonNull(fileStore);
        this.exitAction = Objects.requireNonNull(exitAction);
        this.clock = Objects.requireNonNull(clock);
    }

    // ------------------------------------------------------------------
    // Ciclo de vida de las ventanas
    // ------------------------------------------------------------------

    /**
     * Abre una ventana nueva y la convierte en la ventana actual.
     *
     * @return identificador BGI de la ventana
     */
    public int openWindow(DisplaySpec spec, WindowOptions options) {
        return openWindow(spec, options, windowMode(spec));
    }

    /**
     * Abre una ventana nueva asociada a un modo gráfico BGI (usado por
     * {@code initGraph}) y la convierte en la ventana actual.
     *
     * @return identificador BGI de la ventana
     */
    public int openWindow(DisplaySpec spec, WindowOptions options, GraphicsMode mode) {
        int id = windows.size();
        windows.add(createWindow(id, spec, options, mode));
        currentId = id;
        return id;
    }

    /**
     * Cambia el modo gráfico de una ventana (equivale a {@code setgraphmode}):
     * la pantalla se borra y se restauran los valores por defecto. Si la
     * resolución es la misma se reutiliza la ventana; si cambia, la ventana se
     * recrea con la nueva resolución. En ambos casos conserva su identificador
     * y su modo de escalado.
     */
    public void changeMode(int id, GraphicsMode mode) {
        GraphicsWindow old = find(id).orElseThrow(NoCurrentWindowException::new);
        if (mode.width() == old.spec().width() && mode.height() == old.spec().height()) {
            old.resetGraphMode(mode);
            return;
        }
        DisplaySpec spec = new DisplaySpec(mode.width(), mode.height(), old.spec().title(),
                DisplaySpec.CENTERED, DisplaySpec.CENTERED);
        old.close();
        GraphicsWindow replacement = createWindow(id, spec, old.options(), mode);
        replacement.setScalingMode(old.scalingMode());
        windows.set(id, replacement);
    }

    private GraphicsWindow createWindow(int id, DisplaySpec spec, WindowOptions options, GraphicsMode mode) {
        Display display = platform().openDisplay(spec);
        GraphicsWindow window = new GraphicsWindow(id, display, spec, mode, options, this, glyphs, fileStore);
        window.refreshNow();
        return window;
    }

    /** Modo que se informa para ventanas abiertas con {@code initWindow} (VGA, tamaño libre). */
    private static GraphicsMode windowMode(DisplaySpec spec) {
        GraphicsMode vgaHigh = GraphicsDriver.VGA.highestMode().orElseThrow();
        return new GraphicsMode(GraphicsDriver.VGA, vgaHigh.number(), "VGA", spec.width(), spec.height());
    }

    /** Registra un error que no pertenece a ninguna ventana (por ejemplo, un initGraph fallido). */
    public void reportError(GraphicsError error) {
        pendingError = Objects.requireNonNull(error);
    }

    /**
     * Devuelve y reinicia el último error (equivale a {@code graphresult}).
     * Los errores globales tienen prioridad sobre los de la ventana actual.
     */
    public GraphicsError takeLastError() {
        if (pendingError != GraphicsError.OK) {
            GraphicsError error = pendingError;
            pendingError = GraphicsError.OK;
            return error;
        }
        return find(currentId).map(GraphicsWindow::takeLastError).orElse(GraphicsError.NO_INIT_GRAPH);
    }

    /** Ventana actual. */
    public GraphicsWindow current() {
        return find(currentId).orElseThrow(NoCurrentWindowException::new);
    }

    /** Busca una ventana abierta por su identificador. */
    public Optional<GraphicsWindow> find(int id) {
        if (id < 0 || id >= windows.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(windows.get(id));
    }

    public int currentId() {
        return currentId;
    }

    /** Cambia la ventana actual; los identificadores inválidos se ignoran. */
    public void setCurrent(int id) {
        if (find(id).isPresent()) {
            currentId = id;
        }
    }

    public boolean hasOpenWindows() {
        return windows.stream().anyMatch(Objects::nonNull);
    }

    /** Cierra una ventana concreta. */
    public void close(int id) {
        find(id).ifPresent(window -> {
            window.close();
            windows.set(id, null);
            if (currentId == id) {
                currentId = NO_CURRENT_WINDOW;
            }
        });
        if (!hasOpenWindows()) {
            shutdownPlatform();
        }
    }

    /** Cierra todas las ventanas y libera la plataforma. */
    public void closeAll() {
        for (int id = 0; id < windows.size(); id++) {
            GraphicsWindow window = windows.get(id);
            if (window != null) {
                window.close();
                windows.set(id, null);
            }
        }
        currentId = NO_CURRENT_WINDOW;
        shutdownPlatform();
    }

    // ------------------------------------------------------------------
    // Eventos y refresco
    // ------------------------------------------------------------------

    @Override
    public void requestFrame() {
        if (clock.getAsLong() - lastFrameNanos >= FRAME_INTERVAL_NANOS) {
            processEvents();
        }
    }

    /** Procesa los eventos pendientes y muestra las ventanas con cambios. */
    public void processEvents() {
        if (processing || platform == null) {
            return;
        }
        processing = true;
        try {
            platform.pollEvents(this::dispatch);
            if (exitRequested) {
                // La salida se ejecuta fuera del sondeo para no liberar la
                // plataforma mientras todavía se están leyendo sus eventos.
                exitRequested = false;
                exit();
                return;
            }
            for (GraphicsWindow window : windows) {
                if (window != null) {
                    window.presentIfPending();
                }
            }
            lastFrameNanos = clock.getAsLong();
        } finally {
            processing = false;
        }
    }

    /** Espera el tiempo indicado manteniendo las ventanas activas (equivale a {@code delay}). */
    public void delay(long millis) {
        long deadline = clock.getAsLong() + millis * 1_000_000L;
        processEvents();
        long remaining;
        while ((remaining = deadline - clock.getAsLong()) > 0) {
            sleep(Math.min(POLL_SLEEP_MILLIS, Math.max(1, remaining / 1_000_000L)));
            processEvents();
        }
    }

    /** Espera y devuelve la siguiente tecla de la ventana actual (equivale a {@code getch}). */
    public int readKey() {
        processEvents();
        while (!current().keyboard().hasKey()) {
            sleep(POLL_SLEEP_MILLIS);
            processEvents();
        }
        return current().keyboard().poll();
    }

    /** Indica si hay una tecla pendiente en la ventana actual (equivale a {@code kbhit}). */
    public boolean keyAvailable() {
        processEvents();
        return current().keyboard().hasKey();
    }

    public ScreenSize screenSize() {
        return platform().screenSize();
    }

    /** Muestra un cuadro de error (equivale a {@code showerrorbox}). */
    public void showError(String message) {
        platform().showErrorMessage("Error", message);
    }

    private void dispatch(InputEvent event) {
        switch (event) {
            case InputEvent.QuitRequested ignored -> {
                if (windows.stream().anyMatch(w -> w != null && w.options().exitOnClose())) {
                    exitRequested = true;
                }
            }
            case InputEvent.CloseRequested close -> findByDisplay(close.displayId()).ifPresent(window -> {
                if (window.options().exitOnClose()) {
                    exitRequested = true;
                }
            });
            case InputEvent.KeyTyped key -> route(key.displayId(), event);
            case InputEvent.ExtendedKeyPressed key -> route(key.displayId(), event);
            case InputEvent.MouseMoved move -> route(move.displayId(), event);
            case InputEvent.MouseButtonChanged button -> route(button.displayId(), event);
            case InputEvent.Exposed exposed -> route(exposed.displayId(), event);
            case InputEvent.Resized resized -> route(resized.displayId(), event);
        }
    }

    private void route(int displayId, InputEvent event) {
        findByDisplay(displayId).ifPresent(window -> window.handle(event));
    }

    private Optional<GraphicsWindow> findByDisplay(int displayId) {
        return windows.stream().filter(w -> w != null && w.displayId() == displayId).findFirst();
    }

    private void exit() {
        closeAll();
        exitAction.run();
    }

    private Platform platform() {
        if (platform == null) {
            platform = Objects.requireNonNull(platformFactory.get(), "The platform factory returned null");
        }
        return platform;
    }

    private void shutdownPlatform() {
        if (platform != null) {
            platform.close();
            platform = null;
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
