package com.juniverse.graphics.examples.bgidemo;

import com.juniverse.graphics.examples.exception.ExampleAbortedException;
import com.juniverse.graphics.dto.AspectRatio;
import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.style.LineStyle;
import com.juniverse.graphics.text.FontName;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.text.TextDirection;
import com.juniverse.graphics.text.VerticalAlignment;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Estado y utilidades comunes de la demostración de Borland ({@code BGIDEMO.C}).
 * <p>
 * Reúne las variables globales del original ({@code GraphDriver},
 * {@code MaxX}, {@code MaxColors}, {@code AspectRatio}, ...) y las funciones
 * auxiliares que comparten todas las demostraciones: {@code MainWindow},
 * {@code StatusLine}, {@code DrawBorder}, {@code Pause}, {@code gprintf} y
 * {@code changetextstyle}.
 */
public final class DemoFrame {

    public static final int ESC = 0x1B;

    public static final List<String> FONT_NAMES = List.of(
            "DefaultFont", "TriplexFont", "SmallFont", "SansSerifFont", "GothicFont", "ScriptFont",
            "SimplexFont", "TriplexScriptFont", "ComplexFont", "EuropeanFont", "BoldFont");
    public static final List<String> LINE_STYLE_NAMES = List.of(
            "SolidLn", "DottedLn", "CenterLn", "DashedLn", "UserBitLn");
    public static final List<String> FILL_STYLE_NAMES = List.of(
            "EmptyFill", "SolidFill", "LineFill", "LtSlashFill", "SlashFill", "BkSlashFill",
            "LtBkSlashFill", "HatchFill", "XHatchFill", "InterleaveFill", "WideDotFill", "CloseDotFill");
    public static final List<String> TEXT_DIRECTION_NAMES = List.of("HorizDir", "VertDir");
    public static final List<String> HORIZONTAL_JUSTIFY_NAMES = List.of("LeftText", "CenterText", "RightText");
    public static final List<String> VERTICAL_JUSTIFY_NAMES = List.of("BottomText", "CenterText", "TopText");

    private final int graphDriver;
    private final int graphMode;
    private final int maxX;
    private final int maxY;
    private final int maxColors;
    private final double aspectRatio;
    private final int[] originalPalette;
    private final Random random = new Random();

    private DemoFrame(int graphDriver, int graphMode) {
        this.graphDriver = graphDriver;
        this.graphMode = graphMode;
        this.originalPalette = getPalette();
        this.maxColors = getMaxColor() + 1;
        this.maxX = getMaxX();
        this.maxY = getMaxY();
        AspectRatio ratio = getAspectRatio();
        this.aspectRatio = (double) ratio.x() / ratio.y();
    }

    /**
     * Inicia el modo gráfico con autodetección y lee la configuración
     * (equivale a {@code Initialize}).
     */
    public static DemoFrame initialize() {
        int driver = DETECT;
        initGraph(driver, 0, "");
        int errorCode = graphResult();
        if (errorCode != GR_OK) {
            throw new ExampleAbortedException("Graphics System Error: " + graphErrorMsg(errorCode));
        }
        return new DemoFrame(detectGraph().driver().code(), getGraphMode());
    }

    public int graphDriver() {
        return graphDriver;
    }

    public int graphMode() {
        return graphMode;
    }

    public int maxX() {
        return maxX;
    }

    public int maxY() {
        return maxY;
    }

    public int maxColors() {
        return maxColors;
    }

    public double aspectRatio() {
        return aspectRatio;
    }

    public int[] originalPalette() {
        return originalPalette.clone();
    }

    /** Número aleatorio de 0 a {@code bound - 1} (equivale a {@code random(n)} de Turbo C). */
    public int random(int bound) {
        return bound <= 0 ? 0 : random.nextInt(bound);
    }

    /** Borra la pantalla, dibuja el encabezado y el marco y deja el viewport de trabajo (MainWindow). */
    public void mainWindow(String header) {
        clearDevice();
        setColor(maxColors - 1);
        setViewport(0, 0, maxX, maxY, true);

        int height = textHeight("H");
        changeTextStyle(FontName.DEFAULT, TextDirection.HORIZONTAL, 1);
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        outTextXY(maxX / 2, 2, header);
        setViewport(0, height + 4, maxX, maxY - (height + 4), true);
        drawBorder();
        setViewport(1, height + 5, maxX - 1, maxY - (height + 5), true);
    }

    /** Muestra un mensaje en la línea de estado inferior (StatusLine). */
    public void statusLine(String message) {
        setViewport(0, 0, maxX, maxY, true);
        setColor(maxColors - 1);
        changeTextStyle(FontName.DEFAULT, TextDirection.HORIZONTAL, 1);
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        setLineStyle(LineStyle.SOLID, 0, NORM_WIDTH);
        setFillStyle(FillPattern.EMPTY, 0);

        int height = textHeight("H");
        bar(0, maxY - (height + 4), maxX, maxY);
        rectangle(0, maxY - (height + 4), maxX, maxY);
        outTextXY(maxX / 2, maxY - (height + 2), message);
        setViewport(1, height + 5, maxX - 1, maxY - (height + 5), true);
    }

    /** Dibuja un marco alrededor del viewport actual (DrawBorder). */
    public void drawBorder() {
        setColor(maxColors - 1);
        setLineStyle(LineStyle.SOLID, 0, NORM_WIDTH);
        Viewport viewport = getViewSettings();
        rectangle(0, 0, viewport.right() - viewport.left(), viewport.bottom() - viewport.top());
    }

    /**
     * Espera una tecla (Pause). ESC termina la demostración: en lugar del
     * {@code exit()} del original se lanza {@link ExampleAbortedException},
     * que {@link BgiDemo} atrapa para cerrar la ventana y volver al menú.
     */
    public void pause() {
        statusLine("Esc aborts or press a key...");
        int key = getch();
        if (key == ESC) {
            throw new ExampleAbortedException("BgiDemo aborted with ESC");
        }
        if (key == 0) {
            getch(); // Código de la tecla extendida.
        }
        clearDevice();
    }

    /** Cambia el estilo de texto (changetextstyle). */
    public void changeTextStyle(FontName font, TextDirection direction, int charSize) {
        setTextStyle(font, direction, charSize);
    }

    /**
     * Escribe texto con formato en (x, y) y devuelve la y de la línea
     * siguiente (equivale a gprintf, que recibía x e y por referencia).
     */
    public int print(int x, int y, String format, Object... arguments) {
        outTextXY(x, y, String.format(Locale.ROOT, format, arguments));
        return y + textHeight("H") + 2;
    }
}
