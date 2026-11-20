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
 * Static facade with the classic WinBGI API.
 * <p>
 * Each method is equivalent to a {@code graphics.h} function, named in
 * camelCase ({@code putpixel} → {@link #putPixel}, {@code outtextxy} →
 * {@link #outTextXY}, ...). All operations act on the current window. The
 * facade contains no logic: it delegates to {@link WindowManager} and to
 * the components of {@link GraphicsWindow}.
 * <p>
 * All calls must be made from the same thread.
 */
public final class Bgi {

    /** For {@link #closeGraph(int)}: closes the current window. */
    public static final int CURRENT_WINDOW = -1;
    /** For {@link #closeGraph(int)}: closes all windows. */
    public static final int ALL_WINDOWS = -2;
    /** Value of {@link #getCurrentWindow()} when there is no current window. */
    public static final int NO_CURRENT_WINDOW = WindowManager.NO_CURRENT_WINDOW;
    /** Value meaning "up to the edge" in the image file functions. */
    public static final int TO_EDGE = ImageOperations.TO_EDGE;
    /** Normal line thickness. */
    public static final int NORM_WIDTH = LineSettings.NORMAL_WIDTH;
    /** Thick line thickness. */
    public static final int THICK_WIDTH = LineSettings.THICK_WIDTH;
    /** User-defined character size. */
    public static final int USER_CHAR_SIZE = TextSettings.USER_CHAR_SIZE;

    // ---------------------------------------------------------------------
    // initGraph drivers and modes (same values as graphics.h)
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

    /** Success code of {@link #graphResult()} (grOk). */
    public static final int GR_OK = GraphicsError.OK.code();

    private static final String DEFAULT_TITLE = "Windows BGI";

    private static WindowManager manager = createDefaultManager();

    private Bgi() {
    }

    /**
     * Replaces the window manager (for example, to use another platform
     * or for tests). Must be called before opening windows.
     */
    public static void configure(WindowManager windowManager) {
        if (manager.hasOpenWindows()) {
            throw new IllegalStateException("Cannot reconfigure while windows are open");
        }
        manager = Objects.requireNonNull(windowManager);
    }

    /** Window manager in use, for those who prefer the object-oriented API. */
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
    // Window creation and general control
    // =====================================================================

    /**
     * Initializes the graphics mode as in Borland:
     * <pre>{@code
     * int gd = DETECT, gm = VGA;
     * initGraph(gd, gm, "");
     * }</pre>
     * With {@link #DETECT} the mode is ignored and VGA/VGAHI (640x480) is used. With
     * a specific driver a window is opened with the mode's resolution
     * (for example {@code initGraph(VGA, VGAMED, "")} opens 640x350). The path
     * of the drivers is ignored: no .BGI files are needed.
     * <p>
     * If the driver or the mode are not valid no window is opened and
     * {@link #graphResult()} returns the error code.
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

    /** Equivalent to {@code initGraph(DETECT, 0, "")}: opens a 640x480 VGA window. */
    public static void initGraph() {
        initGraph(DETECT, 0, "");
    }

    private static void openGraphMode(GraphicsMode mode) {
        DisplaySpec spec = DisplaySpec.centered(mode.width(), mode.height(), DEFAULT_TITLE);
        manager.openWindow(spec, WindowOptions.DEFAULT, mode);
    }

    /** Driver and mode that autodetection would choose (equivalent to {@code detectgraph}). */
    public static GraphicsMode detectGraph() {
        return ModeResolver.DETECTED;
    }

    /** Number of the current graphics mode (equivalent to {@code getgraphmode}). */
    public static int getGraphMode() {
        return window().graphicsMode().number();
    }

    /** Highest mode number of the current driver (equivalent to {@code getmaxmode}). */
    public static int getMaxMode() {
        return window().graphicsMode().driver().highestModeNumber();
    }

    /** Description of a mode of the current driver (equivalent to {@code getmodename}). */
    public static String getModeName(int modeNumber) {
        GraphicsDriver driver = window().graphicsMode().driver();
        return driver.mode(modeNumber).map(GraphicsMode::description).orElse("Invalid mode");
    }

    /**
     * Mode range of a driver (equivalent to {@code getmoderange}).
     *
     * @return {@code {lowestMode, highestMode}}, or {@code {-1, -1}} if the driver does not exist
     */
    public static int[] getModeRange(int graphDriver) {
        return GraphicsDriver.fromCode(graphDriver)
                .map(driver -> new int[]{driver.lowestModeNumber(), driver.highestModeNumber()})
                .orElse(new int[]{-1, -1});
    }

    /**
     * Changes the graphics mode of the current window (equivalent to {@code setgraphmode}):
     * the window adopts the mode's resolution, is cleared and goes back to the default
     * values.
     */
    public static void setGraphMode(int graphMode) {
        GraphicsDriver driver = window().graphicsMode().driver();
        driver.mode(graphMode).ifPresentOrElse(
                mode -> manager.changeMode(manager.currentId(), mode),
                () -> window().reportError(GraphicsError.INVALID_MODE));
    }

    /**
     * In BGI this returns to text mode. Here there is no text mode, so the graphics
     * window stays open; it is kept for compatibility.
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
     * Opens a graphics window (equivalent to {@code initwindow}).
     *
     * @param doubleBuffer if true, drawing goes to page 1 and page 0 is displayed
     * @param closeFlag    if true, closing the window terminates the program
     * @return window identifier
     */
    public static int initWindow(int width, int height, String title, int left, int top,
                                 boolean doubleBuffer, boolean closeFlag) {
        return openWindow(new DisplaySpec(width, height, title, left, top),
                new WindowOptions(doubleBuffer, closeFlag));
    }

    private static int openWindow(DisplaySpec spec, WindowOptions options) {
        return manager.openWindow(spec, options);
    }

    /** Closes all windows. */
    public static void closeGraph() {
        closeGraph(ALL_WINDOWS);
    }

    /** Closes one window, the current one ({@link #CURRENT_WINDOW}) or all of them ({@link #ALL_WINDOWS}). */
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

    /** Changes the title of the current window. */
    public static void setWindowTitle(String title) {
        window().setTitle(title);
    }

    public static void graphDefaults() {
        window().graphDefaults();
    }

    /** Returns and resets the code of the last error (equivalent to {@code graphresult}). */
    public static int graphResult() {
        return manager.takeLastError().code();
    }

    public static String graphErrorMsg(int errorCode) {
        return GraphicsError.fromCode(errorCode).message();
    }

    /** Name of the current driver (for example "EGAVGA"), as in Borland. */
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

    /** Waits the given milliseconds without freezing the window. */
    public static void delay(int milliseconds) {
        manager.delay(milliseconds);
    }

    // =====================================================================
    // Dimensions
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

    /** Width of the main screen. */
    public static int getMaxWidth() {
        return manager.screenSize().width();
    }

    /** Height of the main screen. */
    public static int getMaxHeight() {
        return manager.screenSize().height();
    }

    // =====================================================================
    // Drawing
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

    /** Draws a polyline; coordinates are interleaved: x1, y1, x2, y2, ... */
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

    /** Fills a polygon; coordinates are interleaved: x1, y1, x2, y2, ... */
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
    // Colors and styles
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

    /** Defines an 8-row fill pattern (8 bits per row). */
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

    /** Selects the line write mode (COPY or XOR). */
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
    // Refresh and pages
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
    // Text
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
    // Images
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

    /** Draws an image file at its natural size at (0, 0). */
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

    /** Saves the whole viewport to a file (BMP, PNG, JPG or GIF depending on the extension). */
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
    // Keyboard and mouse
    // =====================================================================

    /** Waits for a key. Extended keys return 0 and then their code. */
    public static int getch() {
        return manager.readKey();
    }

    /** Tells whether a key is pending. */
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

    /** Takes the next click of the given kind or {@link Point#NONE} if there is none. */
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

    /** Tells whether the user resized the window (equivalent to {@code isresizeevent}). */
    public static boolean isResizeEvent() {
        manager.processEvents();
        return window().isResizeEvent();
    }

    public static void clearResizeEvent() {
        window().clearResizeEvent();
    }

    /**
     * Defines how the drawing adapts when the window is maximized or resized:
     * {@link ScalingMode#KEEP_ASPECT} (default, keeps the
     * aspect ratio and fills the rest with the background color) or
     * {@link ScalingMode#STRETCH} (covers the whole window, even if it gets distorted).
     */
    public static void setWindowScaling(ScalingMode mode) {
        window().setScalingMode(mode);
    }

    public static ScalingMode getWindowScaling() {
        return window().scalingMode();
    }

    // =====================================================================
    // Palette and color conversion
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

    /** Creates an RGB color code (equivalent to {@code COLOR(r, g, b)}). */
    public static int rgb(int red, int green, int blue) {
        return ColorCode.rgb(red, green, blue);
    }

    public static boolean isBgiColor(int color) {
        return ColorCode.isBgiColor(color);
    }

    public static boolean isRgbColor(int color) {
        return ColorCode.isRgbColor(color);
    }

    /** 0xRRGGBB value of a color code according to the current palette. */
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
    // Internal utilities
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
