package com.juniverse.graphics.window;

import com.juniverse.graphics.dto.WindowOptions;
import com.juniverse.graphics.drawing.DrawingContext;
import com.juniverse.graphics.drawing.DrawingState;
import com.juniverse.graphics.drawing.FillDrawer;
import com.juniverse.graphics.drawing.FillOperations;
import com.juniverse.graphics.drawing.LineDrawer;
import com.juniverse.graphics.drawing.LineOperations;
import com.juniverse.graphics.drawing.ScreenClearer;
import com.juniverse.graphics.drawing.ScreenOperations;
import com.juniverse.graphics.drawing.ShapeDrawer;
import com.juniverse.graphics.drawing.ShapeOperations;
import com.juniverse.graphics.drawing.SurfaceObserver;
import com.juniverse.graphics.dto.Size;
import com.juniverse.graphics.dto.GraphicsMode;
import com.juniverse.graphics.image.ImageFileStore;
import com.juniverse.graphics.image.ImageOperations;
import com.juniverse.graphics.image.ImageTransfer;
import com.juniverse.graphics.dto.InputEvent;
import com.juniverse.graphics.input.KeyboardBuffer;
import com.juniverse.graphics.input.MouseEventKind;
import com.juniverse.graphics.input.MouseTracker;
import com.juniverse.graphics.platform.Display;
import com.juniverse.graphics.dto.DisplaySpec;
import com.juniverse.graphics.platform.ScalingMode;
import com.juniverse.graphics.raster.ViewportCanvas;
import com.juniverse.graphics.text.GlyphRasterizer;
import com.juniverse.graphics.text.TextDrawer;
import com.juniverse.graphics.text.TextOperations;
import com.juniverse.graphics.text.TextState;

import java.util.Objects;

/**
 * BGI graphics window.
 * <p>
 * Composes the pieces of a window (pages, state, painters, keyboard and
 * mouse) and decides when to send the visual page to the screen. Each
 * specific responsibility lives in its own class; this one only connects them.
 */
public final class GraphicsWindow implements SurfaceObserver {

    private final int id;
    private final Display display;
    private final DisplaySpec spec;
    private GraphicsMode mode;
    private final WindowOptions options;
    private final FrameScheduler scheduler;
    private int width;
    private int height;

    private final PageSet pages;
    private final DrawingState drawingState;
    private final TextState textState = new TextState();
    private final KeyboardBuffer keyboard = new KeyboardBuffer();
    private final MouseTracker mouse = new MouseTracker();

    private final LineOperations lines;
    private final ShapeOperations shapes;
    private final FillOperations fills;
    private final ScreenOperations screen;
    private final TextOperations text;
    private final ImageOperations images;

    private boolean refreshing = true;
    private boolean presentPending = true;
    private boolean resizeEvent;
    private ScalingMode scalingMode = ScalingMode.RESIZE;
    private GraphicsError lastError = GraphicsError.OK;

    /**
     * @param id        BGI identifier of the window (index returned by initWindow)
     * @param display   native window where the pages are shown
     * @param spec      parameters the window was created with (they define the drawing area)
     * @param mode      BGI graphics mode associated with the window
     * @param options   behavior options
     * @param scheduler automatic refresh policy
     * @param glyphs    text rasterization engine
     * @param fileStore image file storage
     */
    public GraphicsWindow(int id, Display display, DisplaySpec spec, GraphicsMode mode, WindowOptions options,
                          FrameScheduler scheduler, GlyphRasterizer glyphs, ImageFileStore fileStore) {
        this.id = id;
        this.display = Objects.requireNonNull(display);
        this.spec = Objects.requireNonNull(spec);
        this.mode = Objects.requireNonNull(mode);
        this.options = Objects.requireNonNull(options);
        this.scheduler = Objects.requireNonNull(scheduler);
        this.width = spec.width();
        this.height = spec.height();
        this.drawingState = new DrawingState(width, height);
        this.pages = new PageSet(width, height, drawingState.backgroundArgb());
        if (options.doubleBuffered()) {
            pages.setActive(1);
        }

        ViewportCanvas canvas = new ViewportCanvas(pages::active, drawingState::viewport);
        DrawingContext context = new DrawingContext(drawingState, canvas, this);
        this.lines = new LineDrawer(context);
        this.shapes = new ShapeDrawer(context);
        this.fills = new FillDrawer(context);
        this.screen = new ScreenClearer(context, pages::active);
        this.text = new TextDrawer(context, textState, glyphs);
        this.images = new ImageTransfer(context, fileStore);
    }

    // ------------------------------------------------------------------
    // Identity and components
    // ------------------------------------------------------------------

    public int id() {
        return id;
    }

    /** Native identifier of the window (used to route events). */
    public int displayId() {
        return display.id();
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public WindowOptions options() {
        return options;
    }

    /** Parameters the window was created with. */
    public DisplaySpec spec() {
        return spec;
    }

    /** BGI graphics mode of the window (equivalent to {@code getgraphmode}/{@code getdrivername}). */
    public GraphicsMode graphicsMode() {
        return mode;
    }

    public DrawingState drawingState() {
        return drawingState;
    }

    public LineOperations lines() {
        return lines;
    }

    public ShapeOperations shapes() {
        return shapes;
    }

    public FillOperations fills() {
        return fills;
    }

    public ScreenOperations screen() {
        return screen;
    }

    public TextOperations text() {
        return text;
    }

    public ImageOperations images() {
        return images;
    }

    public KeyboardBuffer keyboard() {
        return keyboard;
    }

    public MouseTracker mouse() {
        return mouse;
    }

    public PageSet pages() {
        return pages;
    }

    public void setTitle(String title) {
        display.setTitle(title);
    }

    // ------------------------------------------------------------------
    // General settings
    // ------------------------------------------------------------------

    /** Restores all default values (equivalent to {@code graphdefaults}). */
    public void graphDefaults() {
        drawingState.reset();
        textState.reset();
        refreshing = true;
    }

    /**
     * Re-enters a graphics mode of the same resolution (equivalent to
     * {@code setgraphmode} without a size change): clears all pages,
     * restores the default values and keeps the native window.
     */
    public void resetGraphMode(GraphicsMode newMode) {
        mode = Objects.requireNonNull(newMode);
        graphDefaults();
        pages.setActive(options.doubleBuffered() ? 1 : 0);
        pages.setVisual(0);
        pages.clearAll(drawingState.backgroundArgb());
        invalidate();
    }

    /** Records an error for {@code graphresult}. */
    public void reportError(GraphicsError error) {
        lastError = Objects.requireNonNull(error);
    }

    /** Returns the last error and resets it to {@link GraphicsError#OK}. */
    public GraphicsError takeLastError() {
        GraphicsError error = lastError;
        lastError = GraphicsError.OK;
        return error;
    }

    // ------------------------------------------------------------------
    // Pages
    // ------------------------------------------------------------------

    public void setActivePage(int page) {
        if (!pages.setActive(page)) {
            reportError(GraphicsError.ERROR);
        }
    }

    public void setVisualPage(int page) {
        if (pages.setVisual(page)) {
            invalidate();
        } else {
            reportError(GraphicsError.ERROR);
        }
    }

    /** Swaps pages 0 and 1 and shows the one just drawn. */
    public void swapBuffers() {
        pages.swap();
        invalidate();
    }

    // ------------------------------------------------------------------
    // Screen refresh
    // ------------------------------------------------------------------

    public boolean isRefreshing() {
        return refreshing;
    }

    /** Enables or disables automatic refresh after each operation. */
    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }

    /** Forces the visual page to be shown on the next cycle. */
    public void invalidate() {
        presentPending = true;
        scheduler.requestFrame();
    }

    /** Shows the visual page immediately. */
    public void refreshNow() {
        presentPending = false;
        display.present(pages.visual(), new Size(width, height), drawingState.backgroundArgb());
    }

    public ScalingMode scalingMode() {
        return scalingMode;
    }

    /** Changes how the page fits the window size and shows it again. */
    public void setScalingMode(ScalingMode scalingMode) {
        this.scalingMode = Objects.requireNonNull(scalingMode);
        display.setScalingMode(scalingMode);
        if (scalingMode == ScalingMode.RESIZE) {
            adaptSurfaceToWindow();
        }
        invalidate();
    }

    /**
     * In {@link ScalingMode#RESIZE} mode, adjusts the visible drawing area to the
     * real size of the window, never going below the initial resolution.
     * <p>
     * The in-memory pages only grow: if the window shrinks or is
     * minimized, only the visible part changes ({@code getMaxX()},
     * {@code getMaxY()}); what was drawn outside it is kept and becomes
     * visible again when the window is enlarged.
     */
    private void adaptSurfaceToWindow() {
        Size output = display.outputSize();
        int newWidth = Math.max(spec.width(), output.width());
        int newHeight = Math.max(spec.height(), output.height());
        if (newWidth == width && newHeight == height) {
            return;
        }
        pages.ensureCapacity(newWidth, newHeight, drawingState.backgroundArgb());
        drawingState.resizeScreen(newWidth, newHeight);
        width = newWidth;
        height = newHeight;
    }

    /** Shows the visual page only if there are pending changes. */
    public void presentIfPending() {
        if (presentPending) {
            refreshNow();
        }
    }

    @Override
    public void surfaceChanged() {
        if (refreshing && pages.isViewingActivePage()) {
            invalidate();
        }
    }

    // ------------------------------------------------------------------
    // Input
    // ------------------------------------------------------------------

    /** Applies a keyboard, mouse or expose event to this window. */
    public void handle(InputEvent event) {
        switch (event) {
            case InputEvent.KeyTyped key -> keyboard.push(key.code());
            case InputEvent.ExtendedKeyPressed key -> keyboard.pushExtended(key.key());
            case InputEvent.MouseMoved move -> mouse.record(MouseEventKind.MOVE, clampX(move.x()), clampY(move.y()));
            case InputEvent.MouseButtonChanged button -> handleButton(button);
            case InputEvent.Exposed ignored -> invalidate();
            case InputEvent.Resized ignored -> {
                resizeEvent = true;
                if (scalingMode == ScalingMode.RESIZE) {
                    adaptSurfaceToWindow();
                }
                invalidate();
            }
            case InputEvent.CloseRequested ignored -> {
                // Closing is handled by WindowManager.
            }
            case InputEvent.QuitRequested ignored -> {
                // Exit is handled by WindowManager.
            }
        }
    }

    /** Tells whether the window changed size (equivalent to {@code isresizeevent}). */
    public boolean isResizeEvent() {
        return resizeEvent;
    }

    /** Discards the resize notice (equivalent to {@code clearresizeevent}). */
    public void clearResizeEvent() {
        resizeEvent = false;
    }

    private void handleButton(InputEvent.MouseButtonChanged button) {
        int x = clampX(button.x());
        int y = clampY(button.y());
        if (!button.pressed()) {
            mouse.record(MouseEventKind.up(button.button()), x, y);
            return;
        }
        mouse.record(MouseEventKind.down(button.button()), x, y);
        if (button.clicks() >= 2) {
            mouse.record(MouseEventKind.doubleClick(button.button()), x, y);
        }
    }

    // When scaling the window, black bands may appear: a click on them
    // is adjusted to the nearest edge of the drawing area.
    private int clampX(int x) {
        return Math.max(0, Math.min(width - 1, x));
    }

    private int clampY(int y) {
        return Math.max(0, Math.min(height - 1, y));
    }

    /** Closes the native window. */
    public void close() {
        display.close();
    }
}
