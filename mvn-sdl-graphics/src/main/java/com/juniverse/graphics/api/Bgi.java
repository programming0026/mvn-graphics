package com.juniverse.graphics.api;

import com.juniverse.graphics.color.BgiColor;
import com.juniverse.graphics.color.ColorCode;
import com.juniverse.graphics.color.Palette;
import com.juniverse.graphics.drawing.DrawingState;
import com.juniverse.graphics.driver.GraphicsDriver;
import com.juniverse.graphics.dto.GraphicsMode;
import com.juniverse.graphics.driver.ModeResolver;
import com.juniverse.graphics.dto.ModeResolution;
import com.juniverse.graphics.dto.ArcCoordinates;
import com.juniverse.graphics.dto.AspectRatio;
import com.juniverse.graphics.dto.Point;
import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.image.BgiImage;
import com.juniverse.graphics.image.ImageIoFileStore;
import com.juniverse.graphics.image.ImageOperations;
import com.juniverse.graphics.input.MouseEventKind;
import com.juniverse.graphics.input.MouseHandler;
import com.juniverse.graphics.dto.DisplaySpec;
import com.juniverse.graphics.platform.ScalingMode;
import com.juniverse.graphics.platform.sdl.SdlPlatform;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.dto.FillSettings;
import com.juniverse.graphics.dto.LineSettings;
import com.juniverse.graphics.style.LineStyle;
import com.juniverse.graphics.style.PatternMask;
import com.juniverse.graphics.style.RasterOperation;
import com.juniverse.graphics.text.FontRoutingRasterizer;
import com.juniverse.graphics.text.FontName;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.text.TextDirection;
import com.juniverse.graphics.dto.TextSettings;
import com.juniverse.graphics.text.VerticalAlignment;
import com.juniverse.graphics.window.GraphicsError;
import com.juniverse.graphics.window.GraphicsWindow;
import com.juniverse.graphics.window.PageSet;
import com.juniverse.graphics.window.WindowManager;
import com.juniverse.graphics.dto.WindowOptions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fachada estática con la API clásica de WinBGI.
 * <p>
 * Cada método equivale a una función de {@code graphics.h} con el nombre en
 * camelCase ({@code putpixel} → {@link #putPixel}, {@code outtextxy} →
 * {@link #outTextXY}, ...). Todas las operaciones actúan sobre la ventana
 * actual. La fachada no contiene lógica: delega en {@link WindowManager} y en
 * los componentes de {@link GraphicsWindow}.
 * <p>
 * Todas las llamadas deben hacerse desde el mismo hilo.
 */
public final class Bgi {

    /** Para {@link #closeGraph(int)}: cierra la ventana actual. */
    public static final int CURRENT_WINDOW = -1;
    /** Para {@link #closeGraph(int)}: cierra todas las ventanas. */
    public static final int ALL_WINDOWS = -2;
    /** Valor de {@link #getCurrentWindow()} cuando no hay ventana actual. */
    public static final int NO_CURRENT_WINDOW = WindowManager.NO_CURRENT_WINDOW;
    /** Valor que indica "hasta el borde" en las funciones de archivos de imagen. */
    public static final int TO_EDGE = ImageOperations.TO_EDGE;
    /** Grosor de línea normal. */
    public static final int NORM_WIDTH = LineSettings.NORMAL_WIDTH;
    /** Grosor de línea grueso. */
    public static final int THICK_WIDTH = LineSettings.THICK_WIDTH;
    /** Tamaño de carácter definido por el usuario. */
    public static final int USER_CHAR_SIZE = TextSettings.USER_CHAR_SIZE;

    // ---------------------------------------------------------------------
    // Controladores y modos de initGraph (mismos valores que graphics.h)
    // ---------------------------------------------------------------------
    public static final int DETECT = GraphicsDriver.DETECT.code();
    public static final int CGA = GraphicsDriver.CGA.code();
    public static final int MCGA = GraphicsDriver.MCGA.code();
    public static final int EGA = GraphicsDriver.EGA.code();
    public static final int EGA64 = GraphicsDriver.EGA64.code();
    public static final int EGAMONO = GraphicsDriver.EGAMONO.code();
    public static final int IBM8514 = GraphicsDriver.IBM8514.code();
    public static final int HERCMONO = GraphicsDriver.HERCMONO.code();
    public static final int ATT400 = GraphicsDriver.ATT400.code();
    public static final int VGA = GraphicsDriver.VGA.code();
    public static final int PC3270 = GraphicsDriver.PC3270.code();

    public static final int CGAC0 = 0;
    public static final int CGAC1 = 1;
    public static final int CGAC2 = 2;
    public static final int CGAC3 = 3;
    public static final int CGAHI = 4;
    public static final int MCGAC0 = 0;
    public static final int MCGAC1 = 1;
    public static final int MCGAC2 = 2;
    public static final int MCGAC3 = 3;
    public static final int MCGAMED = 4;
    public static final int MCGAHI = 5;
    public static final int EGALO = 0;
    public static final int EGAHI = 1;
    public static final int EGA64LO = 0;
    public static final int EGA64HI = 1;
    public static final int EGAMONOHI = 3;
    public static final int HERCMONOHI = 0;
    public static final int ATT400C0 = 0;
    public static final int ATT400C1 = 1;
    public static final int ATT400C2 = 2;
    public static final int ATT400C3 = 3;
    public static final int ATT400MED = 4;
    public static final int ATT400HI = 5;
    public static final int VGALO = 0;
    public static final int VGAMED = 1;
    public static final int VGAHI = 2;
    public static final int PC3270HI = 0;
    public static final int IBM8514LO = 0;
    public static final int IBM8514HI = 1;

    /** Código de éxito de {@link #graphResult()} (grOk). */
    public static final int GR_OK = GraphicsError.OK.code();

    private static final String DEFAULT_TITLE = "Windows BGI";

    private static WindowManager manager = createDefaultManager();

    private Bgi() {
    }

    /**
     * Reemplaza el gestor de ventanas (por ejemplo, para usar otra plataforma
     * o para pruebas). Debe llamarse antes de abrir ventanas.
     */
    public static void configure(WindowManager windowManager) {
        if (manager.hasOpenWindows()) {
            throw new IllegalStateException("Cannot reconfigure while windows are open");
        }
        manager = Objects.requireNonNull(windowManager);
    }

    /** Gestor de ventanas en uso, para quien prefiera la API orientada a objetos. */
    public static WindowManager windowManager() {
        return manager;
    }

    private static WindowManager createDefaultManager() {
        return new WindowManager(SdlPlatform::new, FontRoutingRasterizer.standard(), new ImageIoFileStore(),
                () -> System.exit(0), System::nanoTime);
    }

    private static GraphicsWindow window() {
        return manager.current();
    }

    private static DrawingState state() {
        return window().drawingState();
    }

    // =====================================================================
    // Creación de ventanas y control general
    // =====================================================================

    /**
     * Inicializa el modo gráfico como en Borland:
     * <pre>{@code
     * int gd = DETECT, gm = VGA;
     * initGraph(gd, gm, "");
     * }</pre>
     * Con {@link #DETECT} el modo se ignora y se usa VGA/VGAHI (640x480). Con
     * un controlador concreto se abre una ventana con la resolución del modo
     * (por ejemplo {@code initGraph(VGA, VGAMED, "")} abre 640x350). La ruta
     * de los controladores se ignora: no se necesitan archivos .BGI.
     * <p>
     * Si el controlador o el modo no son válidos no se abre ninguna ventana y
     * {@link #graphResult()} devuelve el código de error.
     */
    public static void initGraph(int graphDriver, int graphMode, String pathToDriver) {
        switch (ModeResolver.resolve(graphDriver, graphMode)) {
            case ModeResolution.Resolved resolved -> openGraphMode(resolved.mode());
            case ModeResolution.Failed failed -> manager.reportError(switch (failed.reason()) {
                case INVALID_DRIVER -> GraphicsError.INVALID_DRIVER;
                case INVALID_MODE -> GraphicsError.INVALID_MODE;
            });
        }
    }

    /** Equivale a {@code initGraph(DETECT, 0, "")}: abre una ventana VGA de 640x480. */
    public static void initGraph() {
        initGraph(DETECT, 0, "");
    }

    private static void openGraphMode(GraphicsMode mode) {
        DisplaySpec spec = DisplaySpec.centered(mode.width(), mode.height(), DEFAULT_TITLE);
        manager.openWindow(spec, WindowOptions.DEFAULT, mode);
    }

    /** Controlador y modo que elegiría la autodetección (equivale a {@code detectgraph}). */
    public static GraphicsMode detectGraph() {
        return ModeResolver.DETECTED;
    }

    /** Número del modo gráfico actual (equivale a {@code getgraphmode}). */
    public static int getGraphMode() {
        return window().graphicsMode().number();
    }

    /** Número de modo más alto del controlador actual (equivale a {@code getmaxmode}). */
    public static int getMaxMode() {
        return window().graphicsMode().driver().highestModeNumber();
    }

    /** Descripción de un modo del controlador actual (equivale a {@code getmodename}). */
    public static String getModeName(int modeNumber) {
        GraphicsDriver driver = window().graphicsMode().driver();
        return driver.mode(modeNumber).map(GraphicsMode::description).orElse("Invalid mode");
    }

    /**
     * Rango de modos de un controlador (equivale a {@code getmoderange}).
     *
     * @return {@code {modoMásBajo, modoMásAlto}}, o {@code {-1, -1}} si el controlador no existe
     */
    public static int[] getModeRange(int graphDriver) {
        return GraphicsDriver.fromCode(graphDriver)
                .map(driver -> new int[]{driver.lowestModeNumber(), driver.highestModeNumber()})
                .orElse(new int[]{-1, -1});
    }

    /**
     * Cambia el modo gráfico de la ventana actual (equivale a {@code setgraphmode}):
     * la ventana adopta la resolución del modo, se borra y vuelve a los valores
     * por defecto.
     */
    public static void setGraphMode(int graphMode) {
        GraphicsDriver driver = window().graphicsMode().driver();
        driver.mode(graphMode).ifPresentOrElse(
                mode -> manager.changeMode(manager.currentId(), mode),
                () -> window().reportError(GraphicsError.INVALID_MODE));
    }

    /**
     * En BGI vuelve al modo texto. Aquí no hay modo texto, así que la ventana
     * gráfica permanece abierta; se mantiene por compatibilidad.
     */
    public static void restoreCrtMode() {
        manager.processEvents();
    }

    public static int initWindow(int width, int height) {
        return initWindow(width, height, DEFAULT_TITLE);
    }

    public static int initWindow(int width, int height, String title) {
        return openWindow(DisplaySpec.centered(width, height, title), WindowOptions.DEFAULT);
    }

    public static int initWindow(int width, int height, String title, int left, int top) {
        return openWindow(new DisplaySpec(width, height, title, left, top), WindowOptions.DEFAULT);
    }

    /**
     * Abre una ventana gráfica (equivale a {@code initwindow}).
     *
     * @param doubleBuffer si es verdadero se dibuja en la página 1 y se muestra la 0
     * @param closeFlag    si es verdadero, cerrar la ventana termina el programa
     * @return identificador de la ventana
     */
    public static int initWindow(int width, int height, String title, int left, int top,
                                 boolean doubleBuffer, boolean closeFlag) {
        return openWindow(new DisplaySpec(width, height, title, left, top),
                new WindowOptions(doubleBuffer, closeFlag));
    }

    private static int openWindow(DisplaySpec spec, WindowOptions options) {
        return manager.openWindow(spec, options);
    }

    /** Cierra todas las ventanas. */
    public static void closeGraph() {
        closeGraph(ALL_WINDOWS);
    }

    /** Cierra una ventana, la actual ({@link #CURRENT_WINDOW}) o todas ({@link #ALL_WINDOWS}). */
    public static void closeGraph(int windowId) {
        switch (windowId) {
            case ALL_WINDOWS -> manager.closeAll();
            case CURRENT_WINDOW -> manager.close(manager.currentId());
            default -> manager.close(windowId);
        }
    }

    public static int getCurrentWindow() {
        return manager.currentId();
    }

    public static void setCurrentWindow(int windowId) {
        manager.setCurrent(windowId);
    }

    /** Cambia el título de la ventana actual. */
    public static void setWindowTitle(String title) {
        window().setTitle(title);
    }

    public static void graphDefaults() {
        window().graphDefaults();
    }

    /** Devuelve y reinicia el código del último error (equivale a {@code graphresult}). */
    public static int graphResult() {
        return manager.takeLastError().code();
    }

    public static String graphErrorMsg(int errorCode) {
        return GraphicsError.fromCode(errorCode).message();
    }

    /** Nombre del controlador actual (por ejemplo "EGAVGA"), como en Borland. */
    public static String getDriverName() {
        return window().graphicsMode().driver().fileName();
    }

    public static AspectRatio getAspectRatio() {
        return state().aspectRatio();
    }

    public static void setAspectRatio(int xAspect, int yAspect) {
        state().setAspectRatio(new AspectRatio(xAspect, yAspect));
    }

    public static void showErrorBox(String message) {
        manager.showError(message);
    }

    /** Espera los milisegundos indicados sin congelar la ventana. */
    public static void delay(int milliseconds) {
        manager.delay(milliseconds);
    }

    // =====================================================================
    // Dimensiones
    // =====================================================================

    public static int getMaxX() {
        return window().width() - 1;
    }

    public static int getMaxY() {
        return window().height() - 1;
    }

    public static int getWindowWidth() {
        return window().width();
    }

    public static int getWindowHeight() {
        return window().height();
    }

    /** Ancho de la pantalla principal. */
    public static int getMaxWidth() {
        return manager.screenSize().width();
    }

    /** Alto de la pantalla principal. */
    public static int getMaxHeight() {
        return manager.screenSize().height();
    }

    // =====================================================================
    // Dibujo
    // =====================================================================

    public static void arc(int x, int y, int startAngle, int endAngle, int radius) {
        window().shapes().arc(x, y, startAngle, endAngle, radius);
    }

    public static void bar(int left, int top, int right, int bottom) {
        window().fills().bar(left, top, right, bottom);
    }

    public static void bar3d(int left, int top, int right, int bottom, int depth, boolean topFlag) {
        window().fills().bar3d(left, top, right, bottom, depth, topFlag);
    }

    public static void circle(int x, int y, int radius) {
        window().shapes().circle(x, y, radius);
    }

    public static void clearDevice() {
        window().screen().clearDevice();
    }

    public static void clearViewport() {
        window().screen().clearViewport();
    }

    /** Dibuja una polilínea; las coordenadas van intercaladas: x1, y1, x2, y2, ... */
    public static void drawPoly(int... coordinates) {
        window().shapes().drawPoly(toPoints(coordinates));
    }

    public static void drawPoly(List<Point> points) {
        window().shapes().drawPoly(points);
    }

    public static void ellipse(int x, int y, int startAngle, int endAngle, int radiusX, int radiusY) {
        window().shapes().ellipse(x, y, startAngle, endAngle, radiusX, radiusY);
    }

    public static void fillEllipse(int x, int y, int radiusX, int radiusY) {
        window().fills().fillEllipse(x, y, radiusX, radiusY);
    }

    /** Rellena un polígono; las coordenadas van intercaladas: x1, y1, x2, y2, ... */
    public static void fillPoly(int... coordinates) {
        window().fills().fillPoly(toPoints(coordinates));
    }

    public static void fillPoly(List<Point> points) {
        window().fills().fillPoly(points);
    }

    public static void floodFill(int x, int y, int borderColor) {
        window().fills().floodFill(x, y, borderColor);
    }

    public static void floodFill(int x, int y, BgiColor borderColor) {
        floodFill(x, y, borderColor.code());
    }

    public static void line(int x1, int y1, int x2, int y2) {
        window().lines().line(x1, y1, x2, y2);
    }

    public static void lineRel(int dx, int dy) {
        window().lines().lineRel(dx, dy);
    }

    public static void lineTo(int x, int y) {
        window().lines().lineTo(x, y);
    }

    public static void moveRel(int dx, int dy) {
        window().lines().moveRel(dx, dy);
    }

    public static void moveTo(int x, int y) {
        window().lines().moveTo(x, y);
    }

    public static int getX() {
        return window().lines().getX();
    }

    public static int getY() {
        return window().lines().getY();
    }

    public static void pieSlice(int x, int y, int startAngle, int endAngle, int radius) {
        window().fills().pieSlice(x, y, startAngle, endAngle, radius);
    }

    public static void putPixel(int x, int y, int color) {
        window().shapes().putPixel(x, y, color);
    }

    public static void putPixel(int x, int y, BgiColor color) {
        putPixel(x, y, color.code());
    }

    public static int getPixel(int x, int y) {
        return window().shapes().getPixel(x, y);
    }

    public static void rectangle(int left, int top, int right, int bottom) {
        window().shapes().rectangle(left, top, right, bottom);
    }

    public static void sector(int x, int y, int startAngle, int endAngle, int radiusX, int radiusY) {
        window().fills().sector(x, y, startAngle, endAngle, radiusX, radiusY);
    }

    public static ArcCoordinates getArcCoords() {
        return state().lastArc();
    }

    // =====================================================================
    // Colores y estilos
    // =====================================================================

    public static void setColor(int color) {
        state().setColor(color);
    }

    public static void setColor(BgiColor color) {
        setColor(color.code());
    }

    public static int getColor() {
        return state().color();
    }

    public static void setBkColor(int color) {
        state().setBackgroundColor(color);
        window().surfaceChanged();
    }

    public static void setBkColor(BgiColor color) {
        setBkColor(color.code());
    }

    public static int getBkColor() {
        return state().backgroundColor();
    }

    public static int getMaxColor() {
        return BgiColor.MAX_COLOR;
    }

    public static void setLineStyle(LineStyle style, int userPattern, int thickness) {
        state().setLineSettings(new LineSettings(style, userPattern, thickness));
    }

    public static LineSettings getLineSettings() {
        return state().lineSettings();
    }

    public static void setFillStyle(FillPattern pattern, int color) {
        if (pattern == FillPattern.USER) {
            window().reportError(GraphicsError.ERROR);
            return;
        }
        state().setFillSettings(new FillSettings(pattern, color));
    }

    public static void setFillStyle(FillPattern pattern, BgiColor color) {
        setFillStyle(pattern, color.code());
    }

    /** Define un patrón de relleno de 8 filas (8 bits por fila). */
    public static void setFillPattern(int[] rows, int color) {
        state().setUserFillPattern(PatternMask.of(rows));
        state().setFillSettings(new FillSettings(FillPattern.USER, color));
    }

    public static int[] getFillPattern() {
        return state().userFillPattern().rows();
    }

    public static FillSettings getFillSettings() {
        return state().fillSettings();
    }

    /** Selecciona el modo de escritura de las líneas (COPY o XOR). */
    public static void setWriteMode(RasterOperation mode) {
        state().setWriteMode(mode);
    }

    public static void setViewport(int left, int top, int right, int bottom, boolean clip) {
        state().setViewport(new Viewport(left, top, right, bottom, clip));
        state().setPosition(new Point(0, 0));
    }

    public static Viewport getViewSettings() {
        return state().viewport();
    }

    // =====================================================================
    // Refresco y páginas
    // =====================================================================

    public static boolean getRefreshingBgi() {
        return window().isRefreshing();
    }

    public static void setRefreshingBgi(boolean value) {
        window().setRefreshing(value);
    }

    public static void refreshBgi(int left, int top, int right, int bottom) {
        refreshAllBgi();
    }

    public static void refreshAllBgi() {
        window().refreshNow();
    }

    public static int getActivePage() {
        return window().pages().activeIndex();
    }

    public static int getVisualPage() {
        return window().pages().visualIndex();
    }

    public static void setActivePage(int page) {
        window().setActivePage(page);
    }

    public static void setVisualPage(int page) {
        window().setVisualPage(page);
    }

    public static void swapBuffers() {
        window().swapBuffers();
    }

    public static int getMaxPages() {
        return PageSet.MAX_PAGES;
    }

    // =====================================================================
    // Texto
    // =====================================================================

    public static void outText(String text) {
        window().text().outText(text);
    }

    public static void outTextXY(int x, int y, String text) {
        window().text().outTextXY(x, y, text);
    }

    public static void setTextStyle(FontName font, TextDirection direction, int charSize) {
        window().text().setTextStyle(font, direction, charSize);
    }

    public static void setTextJustify(HorizontalAlignment horizontal, VerticalAlignment vertical) {
        window().text().setTextJustify(horizontal, vertical);
    }

    public static void setUserCharSize(int multX, int divX, int multY, int divY) {
        window().text().setUserCharSize(multX, divX, multY, divY);
    }

    public static TextSettings getTextSettings() {
        return window().text().textSettings();
    }

    public static int textWidth(String text) {
        return window().text().textWidth(text);
    }

    public static int textHeight(String text) {
        return window().text().textHeight(text);
    }

    // =====================================================================
    // Imágenes
    // =====================================================================

    public static int imageSize(int left, int top, int right, int bottom) {
        return BgiImage.byteSize(right - left + 1, bottom - top + 1);
    }

    public static BgiImage getImage(int left, int top, int right, int bottom) {
        return window().images().getImage(left, top, right, bottom);
    }

    public static void putImage(int left, int top, BgiImage image, RasterOperation operation) {
        window().images().putImage(left, top, image, operation);
    }

    /** Dibuja una imagen de archivo con su tamaño natural en (0, 0). */
    public static void readImageFile(String fileName) {
        readImageFile(fileName, 0, 0, TO_EDGE, TO_EDGE);
    }

    public static void readImageFile(String fileName, int left, int top, int right, int bottom) {
        try {
            window().images().readImageFile(Path.of(fileName), left, top, right, bottom);
        } catch (IOException e) {
            window().reportError(GraphicsError.IO_ERROR);
            throw new UncheckedIOException(e);
        }
    }

    /** Guarda todo el viewport en un archivo (BMP, PNG, JPG o GIF según la extensión). */
    public static void writeImageFile(String fileName) {
        writeImageFile(fileName, 0, 0, TO_EDGE, TO_EDGE);
    }

    public static void writeImageFile(String fileName, int left, int top, int right, int bottom) {
        try {
            window().images().writeImageFile(Path.of(fileName), left, top, right, bottom);
        } catch (IOException e) {
            window().reportError(GraphicsError.IO_ERROR);
            throw new UncheckedIOException(e);
        }
    }

    // =====================================================================
    // Teclado y mouse
    // =====================================================================

    /** Espera una tecla. Las teclas extendidas devuelven 0 y luego su código. */
    public static int getch() {
        return manager.readKey();
    }

    /** Indica si hay una tecla pendiente. */
    public static boolean kbhit() {
        return manager.keyAvailable();
    }

    public static int mouseX() {
        manager.processEvents();
        return window().mouse().x();
    }

    public static int mouseY() {
        manager.processEvents();
        return window().mouse().y();
    }

    public static boolean isMouseClick(MouseEventKind kind) {
        manager.processEvents();
        return window().mouse().hasClick(kind);
    }

    /** Extrae el siguiente clic del tipo indicado o {@link Point#NONE} si no hay. */
    public static Point getMouseClick(MouseEventKind kind) {
        manager.processEvents();
        return window().mouse().pollClick(kind);
    }

    public static void clearMouseClick(MouseEventKind kind) {
        window().mouse().clearClicks(kind);
    }

    public static void registerMouseHandler(MouseEventKind kind, MouseHandler handler) {
        window().mouse().registerHandler(kind, handler);
    }

    public static void setMouseQueueStatus(MouseEventKind kind, boolean enabled) {
        window().mouse().setQueueStatus(kind, enabled);
    }

    /** Indica si el usuario cambió el tamaño de la ventana (equivale a {@code isresizeevent}). */
    public static boolean isResizeEvent() {
        manager.processEvents();
        return window().isResizeEvent();
    }

    public static void clearResizeEvent() {
        window().clearResizeEvent();
    }

    /**
     * Define cómo se ajusta el dibujo cuando la ventana se maximiza o cambia
     * de tamaño: {@link ScalingMode#KEEP_ASPECT} (por defecto, conserva la
     * proporción y rellena el resto con el color de fondo) o
     * {@link ScalingMode#STRETCH} (cubre toda la ventana, aunque se deforme).
     */
    public static void setWindowScaling(ScalingMode mode) {
        window().setScalingMode(mode);
    }

    public static ScalingMode getWindowScaling() {
        return window().scalingMode();
    }

    // =====================================================================
    // Paleta y conversión de colores
    // =====================================================================

    public static int[] getDefaultPalette() {
        return Palette.defaultEntries();
    }

    public static int[] getPalette() {
        return state().palette().entries();
    }

    public static int getPaletteSize() {
        return Palette.SIZE;
    }

    public static void setAllPalette(int[] rgbEntries) {
        state().palette().setAll(rgbEntries);
    }

    public static void setPalette(int colorNumber, int color) {
        state().palette().setEntry(colorNumber, color);
    }

    public static void setRgbPalette(int colorNumber, int red, int green, int blue) {
        state().palette().setRgb(colorNumber, red, green, blue);
    }

    /** Crea un código de color RGB (equivale a {@code COLOR(r, g, b)}). */
    public static int rgb(int red, int green, int blue) {
        return ColorCode.rgb(red, green, blue);
    }

    public static boolean isBgiColor(int color) {
        return ColorCode.isBgiColor(color);
    }

    public static boolean isRgbColor(int color) {
        return ColorCode.isRgbColor(color);
    }

    /** Valor 0xRRGGBB de un código de color según la paleta actual. */
    public static int convertToRgb(int color) {
        return state().palette().toRgb(color);
    }

    public static int redValue(int color) {
        return ColorCode.red(convertToRgb(color));
    }

    public static int greenValue(int color) {
        return ColorCode.green(convertToRgb(color));
    }

    public static int blueValue(int color) {
        return ColorCode.blue(convertToRgb(color));
    }

    // =====================================================================
    // Utilidades internas
    // =====================================================================

    private static List<Point> toPoints(int[] coordinates) {
        if (coordinates.length % 2 != 0) {
            throw new IllegalArgumentException("Coordinates must come in (x, y) pairs");
        }
        List<Point> points = new ArrayList<>(coordinates.length / 2);
        for (int i = 0; i < coordinates.length; i += 2) {
            points.add(new Point(coordinates[i], coordinates[i + 1]));
        }
        return points;
    }
}
