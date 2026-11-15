package com.juniverse.graphics.window;

import com.juniverse.graphics.dto.WindowOptions;
import com.juniverse.graphics.exception.NoCurrentWindowException;
import com.juniverse.graphics.color.BgiColor;
import com.juniverse.graphics.color.ColorCode;
import com.juniverse.graphics.driver.GraphicsDriver;
import com.juniverse.graphics.dto.GraphicsMode;
import com.juniverse.graphics.dto.Point;
import com.juniverse.graphics.dto.Size;
import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.image.BgiImage;
import com.juniverse.graphics.image.ImageIoFileStore;
import com.juniverse.graphics.input.ExtendedKey;
import com.juniverse.graphics.dto.InputEvent;
import com.juniverse.graphics.input.MouseButton;
import com.juniverse.graphics.input.MouseEventKind;
import com.juniverse.graphics.dto.DisplaySpec;
import com.juniverse.graphics.platform.ScalingMode;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.dto.FillSettings;
import com.juniverse.graphics.style.RasterOperation;
import com.juniverse.graphics.testing.FakePlatform;
import com.juniverse.graphics.text.FontRoutingRasterizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphicsWindowTest {

    private FakePlatform platform;
    private WindowManager manager;
    private GraphicsWindow window;
    private boolean exited;

    @BeforeEach
    public void setUp() {
        platform = new FakePlatform();
        manager = new WindowManager(() -> platform, FontRoutingRasterizer.standard(), new ImageIoFileStore(),
                () -> exited = true, System::nanoTime);
        manager.openWindow(DisplaySpec.centered(200, 100, "test"), WindowOptions.DEFAULT);
        window = manager.current();
    }

    @Test
    public void putPixelAndGetPixelUseColorCodes() {
        window.shapes().putPixel(10, 10, BgiColor.YELLOW.code());
        window.shapes().putPixel(11, 10, ColorCode.rgb(1, 2, 3));

        assertEquals(BgiColor.YELLOW.code(), window.shapes().getPixel(10, 10));
        assertEquals(ColorCode.rgb(1, 2, 3), window.shapes().getPixel(11, 10));
        assertEquals(BgiColor.BLACK.code(), window.shapes().getPixel(0, 0));
    }

    @Test
    public void xorRectangleDrawnTwiceRestoresBackground() {
        window.drawingState().setWriteMode(RasterOperation.XOR);
        window.shapes().rectangle(10, 10, 50, 40);
        assertEquals(BgiColor.WHITE.code(), window.shapes().getPixel(10, 10));
        window.shapes().rectangle(10, 10, 50, 40);
        assertEquals(BgiColor.BLACK.code(), window.shapes().getPixel(10, 10));
        assertEquals(BgiColor.BLACK.code(), window.shapes().getPixel(50, 40));
    }

    @Test
    public void barUsesFillColorAndViewportOffset() {
        window.drawingState().setViewport(new Viewport(20, 20, 120, 80, true));
        window.drawingState().setFillSettings(new FillSettings(FillPattern.SOLID, BgiColor.RED.code()));
        window.fills().bar(0, 0, 5, 5);

        assertEquals(BgiColor.RED.code(), window.shapes().getPixel(0, 0));
        window.drawingState().setViewport(Viewport.fullScreen(200, 100));
        assertEquals(BgiColor.RED.code(), window.shapes().getPixel(20, 20));
        assertEquals(BgiColor.BLACK.code(), window.shapes().getPixel(19, 19));
    }

    @Test
    public void floodFillFillsClosedCircle() {
        window.shapes().circle(100, 50, 20);
        window.drawingState().setFillSettings(new FillSettings(FillPattern.SOLID, BgiColor.GREEN.code()));
        window.fills().floodFill(100, 50, BgiColor.WHITE.code());

        assertEquals(BgiColor.GREEN.code(), window.shapes().getPixel(100, 50));
        assertEquals(BgiColor.BLACK.code(), window.shapes().getPixel(150, 50));
    }

    @Test
    public void lineToMovesCurrentPosition() {
        window.lines().moveTo(5, 5);
        window.lines().lineTo(30, 5);
        window.lines().lineRel(0, 10);

        assertEquals(30, window.lines().getX());
        assertEquals(15, window.lines().getY());
        assertEquals(BgiColor.WHITE.code(), window.shapes().getPixel(20, 5));
        assertEquals(BgiColor.WHITE.code(), window.shapes().getPixel(30, 12));
    }

    @Test
    public void textDrawsPixelsAndAdvancesPosition() {
        int width = window.text().textWidth("Hola");
        window.text().outText("Hola");

        assertTrue(width > 0);
        assertEquals(width, window.lines().getX());
        boolean anyPixel = false;
        for (int y = 0; y < window.text().textHeight("Hola"); y++) {
            for (int x = 0; x < width; x++) {
                anyPixel |= window.shapes().getPixel(x, y) == BgiColor.WHITE.code();
            }
        }
        assertTrue(anyPixel);
    }

    @Test
    public void getImageAndPutImageCopyPixels() {
        window.shapes().putPixel(1, 1, BgiColor.CYAN.code());
        BgiImage image = window.images().getImage(0, 0, 2, 2);
        window.images().putImage(100, 50, image, RasterOperation.COPY);

        assertEquals(3, image.width());
        assertEquals(BgiColor.CYAN.code(), window.shapes().getPixel(101, 51));
    }

    @Test
    public void imageFilesRoundTrip(@TempDir Path folder) throws Exception {
        window.shapes().putPixel(3, 4, BgiColor.LIGHT_RED.code());
        Path file = folder.resolve("captura.png");
        window.images().writeImageFile(file, 0, 0, 9, 9);
        window.screen().clearDevice();
        window.images().readImageFile(file, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);

        assertEquals(BgiColor.LIGHT_RED.code(), window.shapes().getPixel(3, 4));
    }

    @Test
    public void swapBuffersAlternatesPages() {
        window.swapBuffers();
        assertEquals(1, window.pages().activeIndex());
        assertEquals(0, window.pages().visualIndex());
        window.swapBuffers();
        assertEquals(0, window.pages().activeIndex());
        assertEquals(1, window.pages().visualIndex());
    }

    @Test
    public void eventsAreRoutedToWindow() {
        int displayId = window.displayId();
        platform.enqueue(new InputEvent.KeyTyped(displayId, 'x'));
        platform.enqueue(new InputEvent.ExtendedKeyPressed(displayId, ExtendedKey.F1));
        platform.enqueue(new InputEvent.MouseButtonChanged(displayId, MouseButton.LEFT, true, 2, 4, 5));

        assertEquals('x', manager.readKey());
        assertEquals(0, manager.readKey());
        assertEquals(ExtendedKey.F1.code(), manager.readKey());
        assertEquals(new Point(4, 5), window.mouse().pollClick(MouseEventKind.LEFT_BUTTON_DOWN));
        assertTrue(window.mouse().hasClick(MouseEventKind.LEFT_BUTTON_DOUBLE_CLICK));
    }

    @Test
    public void closingWithExitFlagRunsExitAction() {
        platform.enqueue(new InputEvent.CloseRequested(window.displayId()));
        manager.processEvents();

        assertTrue(exited);
        assertFalse(manager.hasOpenWindows());
        assertTrue(platform.isClosed());
        assertThrows(NoCurrentWindowException.class, manager::current);
    }

    @Test
    public void drawingRefreshesDisplayWhenRefreshingIsEnabled() {
        FakePlatform.FakeDisplay display = platform.displays().get(0);
        int before = display.presentCount();
        window.fills().fillPoly(List.of(new Point(0, 0), new Point(20, 0), new Point(10, 20)));
        manager.processEvents();
        assertTrue(display.presentCount() > before);

        window.setRefreshing(false);
        manager.processEvents();
        int afterDisable = display.presentCount();
        window.lines().line(0, 0, 10, 10);
        manager.processEvents();
        assertEquals(afterDisable, display.presentCount());
    }

    @Test
    public void resizeIsReportedAndForcesRepaint() {
        FakePlatform.FakeDisplay display = platform.displays().get(0);
        window.setRefreshing(false);
        manager.processEvents();
        int before = display.presentCount();

        platform.enqueue(new InputEvent.Resized(window.displayId()));
        manager.processEvents();

        assertTrue(window.isResizeEvent());
        assertTrue(display.presentCount() > before);
        window.clearResizeEvent();
        assertFalse(window.isResizeEvent());
    }

    @Test
    public void mouseOutsideDrawingAreaIsClamped() {
        platform.enqueue(new InputEvent.MouseMoved(window.displayId(), -15, 500));
        manager.processEvents();

        assertEquals(0, window.mouse().x());
        assertEquals(99, window.mouse().y());
    }

    @Test
    public void changeModeRecreatesWindowWithModeResolution() {
        GraphicsMode egaHigh = GraphicsDriver.EGA.mode(1).orElseThrow();
        manager.changeMode(window.id(), egaHigh);
        GraphicsWindow changed = manager.current();

        assertEquals(window.id(), changed.id());
        assertEquals(640, changed.width());
        assertEquals(350, changed.height());
        assertEquals(egaHigh, changed.graphicsMode());
        assertTrue(platform.displays().get(0).isClosed());
    }

    @Test
    public void windowBorderUsesBackgroundColorAndScalingModeIsForwarded() {
        FakePlatform.FakeDisplay display = platform.displays().get(0);
        window.drawingState().setBackgroundColor(BgiColor.BLUE.code());
        window.refreshNow();
        assertEquals(0xFF0000A8, display.lastBorderArgb());

        window.setScalingMode(ScalingMode.STRETCH);
        assertEquals(ScalingMode.STRETCH, display.scalingMode());
        assertEquals(ScalingMode.STRETCH, window.scalingMode());
    }

    @Test
    public void resizeModeGrowsDrawingAreaWithWindowAndKeepsDrawing() {
        FakePlatform.FakeDisplay display = platform.displays().get(0);
        window.shapes().putPixel(5, 5, BgiColor.YELLOW.code());

        display.setOutputSize(new Size(400, 300));
        platform.enqueue(new InputEvent.Resized(window.displayId()));
        manager.processEvents();

        assertEquals(400, window.width());
        assertEquals(300, window.height());
        assertEquals(new Viewport(0, 0, 399, 299, false), window.drawingState().viewport());
        assertEquals(BgiColor.YELLOW.code(), window.shapes().getPixel(5, 5));
        assertEquals(new Size(400, 300), display.lastPageSize());

        // Al restaurar una ventana más chica, el área no baja de la resolución inicial.
        display.setOutputSize(new Size(100, 50));
        platform.enqueue(new InputEvent.Resized(window.displayId()));
        manager.processEvents();
        assertEquals(200, window.width());
        assertEquals(100, window.height());
        assertEquals(new Size(200, 100), display.lastPageSize());
    }

    @Test
    public void drawingOutsideVisibleAreaSurvivesShrinkingAndRegrowingWindow() {
        FakePlatform.FakeDisplay display = platform.displays().get(0);
        display.setOutputSize(new Size(400, 300));
        platform.enqueue(new InputEvent.Resized(window.displayId()));
        manager.processEvents();
        window.shapes().rectangle(300, 200, 350, 250);

        // Restaurar o minimizar: solo se achica la parte visible.
        display.setOutputSize(new Size(0, 0));
        platform.enqueue(new InputEvent.Resized(window.displayId()));
        manager.processEvents();
        assertEquals(200, window.width());

        // Al maximizar de nuevo, el rectángulo sigue ahí.
        display.setOutputSize(new Size(400, 300));
        platform.enqueue(new InputEvent.Resized(window.displayId()));
        manager.processEvents();
        assertEquals(400, window.width());
        assertEquals(BgiColor.WHITE.code(), window.shapes().getPixel(300, 200));
        assertEquals(BgiColor.WHITE.code(), window.shapes().getPixel(350, 250));
    }

    @Test
    public void otherScalingModesKeepLogicalResolution() {
        FakePlatform.FakeDisplay display = platform.displays().get(0);
        window.setScalingMode(ScalingMode.KEEP_ASPECT);

        display.setOutputSize(new Size(400, 300));
        platform.enqueue(new InputEvent.Resized(window.displayId()));
        manager.processEvents();

        assertEquals(200, window.width());
        assertEquals(100, window.height());
    }

    @Test
    public void changeModeWithSameResolutionReusesWindowAndKeepsScaling() {
        window.setScalingMode(ScalingMode.KEEP_ASPECT);
        window.shapes().putPixel(5, 5, BgiColor.YELLOW.code());
        window.drawingState().setColor(BgiColor.RED.code());
        GraphicsMode sameSize = new GraphicsMode(GraphicsDriver.VGA, 2, "VGA", 200, 100);

        manager.changeMode(window.id(), sameSize);

        assertEquals(window, manager.current());
        assertFalse(platform.displays().get(0).isClosed());
        assertEquals(ScalingMode.KEEP_ASPECT, window.scalingMode());
        assertEquals(BgiColor.BLACK.code(), window.shapes().getPixel(5, 5));
        assertEquals(BgiColor.WHITE.code(), window.drawingState().color());
    }

    @Test
    public void globalErrorsHavePriorityInGraphResult() {
        manager.reportError(GraphicsError.INVALID_MODE);

        assertEquals(GraphicsError.INVALID_MODE, manager.takeLastError());
        assertEquals(GraphicsError.OK, manager.takeLastError());
    }
}
