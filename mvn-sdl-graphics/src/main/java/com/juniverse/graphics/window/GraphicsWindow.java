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
 * Ventana gráfica BGI.
 * <p>
 * Compone las piezas de una ventana (páginas, estado, dibujantes, teclado y
 * mouse) y decide cuándo enviar la página visual a la pantalla. Cada
 * responsabilidad concreta vive en su propia clase; esta solo las conecta.
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
     * @param id        identificador BGI de la ventana (índice devuelto por initWindow)
     * @param display   ventana nativa donde se muestran las páginas
     * @param spec      parámetros con los que se creó la ventana (definen el área de dibujo)
     * @param mode      modo gráfico BGI asociado a la ventana
     * @param options   opciones de comportamiento
     * @param scheduler política de refresco automático
     * @param glyphs    motor de rasterizado de texto
     * @param fileStore almacenamiento de archivos de imagen
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
    // Identidad y componentes
    // ------------------------------------------------------------------

    public int id() {
        return id;
    }

    /** Identificador nativo de la ventana (usado para enrutar eventos). */
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

    /** Parámetros con los que se creó la ventana. */
    public DisplaySpec spec() {
        return spec;
    }

    /** Modo gráfico BGI de la ventana (equivale a {@code getgraphmode}/{@code getdrivername}). */
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
    // Configuración general
    // ------------------------------------------------------------------

    /** Restaura todos los valores por defecto (equivale a {@code graphdefaults}). */
    public void graphDefaults() {
        drawingState.reset();
        textState.reset();
        refreshing = true;
    }

    /**
     * Vuelve a entrar en un modo gráfico de la misma resolución (equivale a
     * {@code setgraphmode} sin cambio de tamaño): borra todas las páginas,
     * restaura los valores por defecto y conserva la ventana nativa.
     */
    public void resetGraphMode(GraphicsMode newMode) {
        mode = Objects.requireNonNull(newMode);
        graphDefaults();
        pages.setActive(options.doubleBuffered() ? 1 : 0);
        pages.setVisual(0);
        pages.clearAll(drawingState.backgroundArgb());
        invalidate();
    }

    /** Registra un error para {@code graphresult}. */
    public void reportError(GraphicsError error) {
        lastError = Objects.requireNonNull(error);
    }

    /** Devuelve el último error y lo reinicia a {@link GraphicsError#OK}. */
    public GraphicsError takeLastError() {
        GraphicsError error = lastError;
        lastError = GraphicsError.OK;
        return error;
    }

    // ------------------------------------------------------------------
    // Páginas
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

    /** Alterna las páginas 0 y 1 y muestra la recién dibujada. */
    public void swapBuffers() {
        pages.swap();
        invalidate();
    }

    // ------------------------------------------------------------------
    // Refresco de pantalla
    // ------------------------------------------------------------------

    public boolean isRefreshing() {
        return refreshing;
    }

    /** Activa o desactiva el refresco automático tras cada operación. */
    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }

    /** Fuerza que la página visual se muestre en el próximo ciclo. */
    public void invalidate() {
        presentPending = true;
        scheduler.requestFrame();
    }

    /** Muestra inmediatamente la página visual. */
    public void refreshNow() {
        presentPending = false;
        display.present(pages.visual(), new Size(width, height), drawingState.backgroundArgb());
    }

    public ScalingMode scalingMode() {
        return scalingMode;
    }

    /** Cambia cómo se ajusta la página al tamaño de la ventana y la vuelve a mostrar. */
    public void setScalingMode(ScalingMode scalingMode) {
        this.scalingMode = Objects.requireNonNull(scalingMode);
        display.setScalingMode(scalingMode);
        if (scalingMode == ScalingMode.RESIZE) {
            adaptSurfaceToWindow();
        }
        invalidate();
    }

    /**
     * En modo {@link ScalingMode#RESIZE}, ajusta el área visible de dibujo al
     * tamaño real de la ventana, sin bajar nunca de la resolución inicial.
     * <p>
     * Las páginas en memoria solo crecen: si la ventana se reduce o se
     * minimiza, únicamente cambia la parte visible ({@code getMaxX()},
     * {@code getMaxY()}); lo dibujado fuera de ella se conserva y vuelve a
     * verse al agrandar la ventana.
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

    /** Muestra la página visual solo si hay cambios pendientes. */
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
    // Entrada
    // ------------------------------------------------------------------

    /** Aplica un evento de teclado, mouse o exposición a esta ventana. */
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
                // El cierre lo gestiona WindowManager.
            }
            case InputEvent.QuitRequested ignored -> {
                // La salida la gestiona WindowManager.
            }
        }
    }

    /** Indica si la ventana cambió de tamaño (equivale a {@code isresizeevent}). */
    public boolean isResizeEvent() {
        return resizeEvent;
    }

    /** Descarta el aviso de cambio de tamaño (equivale a {@code clearresizeevent}). */
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

    // Al escalar la ventana pueden aparecer bandas negras: un clic sobre ellas
    // se ajusta al borde más cercano del área de dibujo.
    private int clampX(int x) {
        return Math.max(0, Math.min(width - 1, x));
    }

    private int clampY(int y) {
        return Math.max(0, Math.min(height - 1, y));
    }

    /** Cierra la ventana nativa. */
    public void close() {
        display.close();
    }
}
