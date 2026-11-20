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
 * Manages the open graphics windows, the current window and the
 * event cycle.
 * <p>
 * BGI is an immediate-mode, blocking API ({@code getch}, {@code delay}),
 * so there is no separate UI thread: events are processed on the
 * program's thread every time it waits or, at most once per
 * frame (about 60 FPS), while it draws. This way SDL is always used from a single thread.
 */
public final class WindowManager implements FrameScheduler {

    /** Minimum interval between automatic refreshes (about 60 frames per second). */
    private static final long FRAME_INTERVAL_NANOS = 1_000_000_000L / 60;

    /** Maximum pause between event polls while waiting. */
    private static final long POLL_SLEEP_MILLIS = 5;

    /** Identifier meaning there is no current window. */
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
     * @param platformFactory creates the platform the first time a window is opened
     * @param glyphs          text engine shared by the windows
     * @param fileStore       image storage shared by the windows
     * @param exitAction      action run when the user closes a window with
     *                        {@link WindowOptions#exitOnClose()} (normally {@code System.exit})
     * @param clock           monotonic clock in nanoseconds
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
    // Window lifecycle
    // ------------------------------------------------------------------

    /**
     * Opens a new window and makes it the current window.
     *
     * @return BGI identifier of the window
     */
    public int openWindow(DisplaySpec spec, WindowOptions options) {
        return openWindow(spec, options, windowMode(spec));
    }

    /**
     * Opens a new window associated with a BGI graphics mode (used by
     * {@code initGraph}) and makes it the current window.
     *
     * @return BGI identifier of the window
     */
    public int openWindow(DisplaySpec spec, WindowOptions options, GraphicsMode mode) {
        int id = windows.size();
        windows.add(createWindow(id, spec, options, mode));
        currentId = id;
        return id;
    }

    /**
     * Changes the graphics mode of a window (equivalent to {@code setgraphmode}):
     * the screen is cleared and the default values are restored. If the
     * resolution is the same the window is reused; if it changes, the window is
     * recreated with the new resolution. In both cases it keeps its identifier
     * and its scaling mode.
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

    /** Mode reported for windows opened with {@code initWindow} (VGA, free size). */
    private static GraphicsMode windowMode(DisplaySpec spec) {
        GraphicsMode vgaHigh = GraphicsDriver.VGA.highestMode().orElseThrow();
        return new GraphicsMode(GraphicsDriver.VGA, vgaHigh.number(), "VGA", spec.width(), spec.height());
    }

    /** Records an error that does not belong to any window (for example, a failed initGraph). */
    public void reportError(GraphicsError error) {
        pendingError = Objects.requireNonNull(error);
    }

    /**
     * Returns and resets the last error (equivalent to {@code graphresult}).
     * Global errors take priority over those of the current window.
     */
    public GraphicsError takeLastError() {
        if (pendingError != GraphicsError.OK) {
            GraphicsError error = pendingError;
            pendingError = GraphicsError.OK;
            return error;
        }
        return find(currentId).map(GraphicsWindow::takeLastError).orElse(GraphicsError.NO_INIT_GRAPH);
    }

    /** Current window. */
    public GraphicsWindow current() {
        return find(currentId).orElseThrow(NoCurrentWindowException::new);
    }

    /** Looks up an open window by its identifier. */
    public Optional<GraphicsWindow> find(int id) {
        if (id < 0 || id >= windows.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(windows.get(id));
    }

    public int currentId() {
        return currentId;
    }

    /** Changes the current window; invalid identifiers are ignored. */
    public void setCurrent(int id) {
        if (find(id).isPresent()) {
            currentId = id;
        }
    }

    public boolean hasOpenWindows() {
        return windows.stream().anyMatch(Objects::nonNull);
    }

    /** Closes a specific window. */
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

    /** Closes all windows and releases the platform. */
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
    // Events and refresh
    // ------------------------------------------------------------------

    @Override
    public void requestFrame() {
        if (clock.getAsLong() - lastFrameNanos >= FRAME_INTERVAL_NANOS) {
            processEvents();
        }
    }

    /** Processes pending events and shows the windows with changes. */
    public void processEvents() {
        if (processing || platform == null) {
            return;
        }
        processing = true;
        try {
            platform.pollEvents(this::dispatch);
            if (exitRequested) {
                // Exit runs outside the polling so as not to release the
                // platform while its events are still being read.
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

    /** Waits the given time keeping the windows alive (equivalent to {@code delay}). */
    public void delay(long millis) {
        long deadline = clock.getAsLong() + millis * 1_000_000L;
        processEvents();
        long remaining;
        while ((remaining = deadline - clock.getAsLong()) > 0) {
            sleep(Math.min(POLL_SLEEP_MILLIS, Math.max(1, remaining / 1_000_000L)));
            processEvents();
        }
    }

    /** Waits for and returns the next key of the current window (equivalent to {@code getch}). */
    public int readKey() {
        processEvents();
        while (!current().keyboard().hasKey()) {
            sleep(POLL_SLEEP_MILLIS);
            processEvents();
        }
        return current().keyboard().poll();
    }

    /** Tells whether a key is pending in the current window (equivalent to {@code kbhit}). */
    public boolean keyAvailable() {
        processEvents();
        return current().keyboard().hasKey();
    }

    public ScreenSize screenSize() {
        return platform().screenSize();
    }

    /** Shows an error box (equivalent to {@code showerrorbox}). */
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
