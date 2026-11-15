package com.juniverse.graphics.examples.mathdemo;

import com.juniverse.graphics.color.BgiColor;
import com.juniverse.graphics.dto.Point;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.text.FontName;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.text.TextDirection;
import com.juniverse.graphics.text.VerticalAlignment;

import java.math.BigDecimal;
import java.math.MathContext;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Port de {@code GRAFICA.H}: pequeña biblioteca para graficar funciones en
 * coordenadas lógicas (las del problema matemático) sobre la pantalla.
 * <p>
 * Las variables globales del original ({@code W1..W4}, {@code V1..V4},
 * {@code U1}, {@code U2}) pasan a ser el estado de una instancia: la
 * "ventana lógica" que se transforma en coordenadas de pantalla.
 * <p>
 * Equivalencias: {@code VentanaLogica} → {@link #setLogicalWindow},
 * {@code Transforma} → {@link #toScreen}, {@code CoordenadasXY} →
 * {@link #drawCoordinates}, {@code Plot2D} → {@link #plot}, etc.
 */
public final class Plot2D {

    /** Número de divisiones de cada eje. */
    private static final double DIVISIONS = 10.0;
    /** Las marcas de los ejes miden 1/50 del rango. */
    private static final double TICK_RATIO = 50.0;
    /** Dígitos significativos de las etiquetas (como {@code gcvt(n, 3, ...)}). */
    private static final MathContext LABEL_PRECISION = new MathContext(3);
    /** Separación en píxeles entre una marca del eje y su número. */
    private static final int LABEL_GAP = 3;

    // Ventana lógica.
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    // Ventana de pantalla y factores de escala.
    private int screenLeft;
    private int screenRight;
    private int screenTop;
    private int screenBottom;
    private double scaleX;
    private double scaleY;

    /** Equivale a {@code ClosePlot2D}: espera una tecla y cierra la ventana gráfica. */
    public void close() {
        getch();
        closeGraph();
    }

    /**
     * Prepara la ventana lógica, dibuja los ejes y escribe sus valores
     * (equivale a {@code CoordenadasXY}).
     */
    public void drawCoordinates(double a, double b, double yMin, double yMax) {
        setLogicalWindow(a, yMin, b, yMax);
        drawAxes(a, b, yMin, yMax);
        drawAxisLabels(a, b, yMin, yMax);
    }

    /**
     * Define el rectángulo lógico que ocupa toda la pantalla (equivale a
     * {@code VentanaLogica}).
     */
    public void setLogicalWindow(double x1, double y1, double x2, double y2) {
        screenLeft = 0;
        screenRight = getMaxX();
        screenTop = 0;
        screenBottom = getMaxY();
        minX = x1;
        maxX = x2;
        minY = y1;
        maxY = y2;
        scaleX = (screenRight - screenLeft) / (maxX - minX);
        scaleY = (screenBottom - screenTop) / (maxY - minY);
    }

    /** Convierte un punto lógico a coordenadas de pantalla (equivale a {@code Transforma}). */
    public Point toScreen(double x, double y) {
        int screenX = (int) ((x - minX) * scaleX);
        int screenY = (int) ((screenBottom - screenTop) - (y - minY) * scaleY);
        return new Point(screenX, screenY);
    }

    /**
     * Dibuja un punto de la gráfica unido con el anterior (equivale a
     * {@code Plot2D}). Los puntos fuera de la pantalla se ignoran.
     */
    public void plot(double x, double y, BgiColor color) {
        Point point = toScreen(x, y);
        boolean visible = screenLeft < point.x() && point.x() < screenRight
                && screenTop < point.y() && point.y() < screenBottom;
        if (!visible) {
            return;
        }

        if (getColor() != color.code()) {
            moveTo(point.x(), point.y());
        }
        setColor(color);
        lineTo(point.x(), point.y());
    }

    /** Dibuja un segmento en coordenadas lógicas (equivale a {@code Line}). */
    public void line(double x0, double y0, double x1, double y1, BgiColor color) {
        setColor(color);
        Point start = toScreen(x0, y0);
        Point end = toScreen(x1, y1);
        com.juniverse.graphics.api.Bgi.line(start.x(), start.y(), end.x(), end.y());
    }

    /** Dibuja un rectángulo en coordenadas lógicas (equivale a {@code Rectangle}). */
    public void rectangle(double x1, double y1, double x2, double y2, BgiColor color) {
        setColor(color);
        Point corner1 = toScreen(x1, y1);
        Point corner2 = toScreen(x2, y2);
        com.juniverse.graphics.api.Bgi.rectangle(corner1.x(), corner1.y(), corner2.x(), corner2.y());
    }

    /** Escribe un texto en una posición lógica (equivale a {@code Letrero}). */
    public void label(double x, double y, String message, BgiColor color, TextDirection direction) {
        setTextStyle(FontName.DEFAULT, direction, 1);
        setColor(color);
        Point point = toScreen(x, y);
        outTextXY(point.x(), point.y(), message);
    }

    /**
     * Escribe un título en una posición lógica (equivale a {@code Title}).
     * <p>
     * El original calculaba la posición en pantalla pero escribía con las
     * coordenadas lógicas ({@code outtextxy(x, y, ...)}), por lo que el
     * título quedaba en la esquina superior izquierda. Aquí se usa la
     * posición transformada, que era la intención.
     */
    public void title(double x, double y, String message, BgiColor color) {
        setColor(color);
        Point point = toScreen(x, y);
        setTextStyle(FontName.SMALL, TextDirection.HORIZONTAL, 4);
        setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        outTextXY(point.x(), point.y(), message);
    }

    /** Fondo azul, marco, ejes y marcas (equivale a {@code Ejes}). */
    private void drawAxes(double a, double b, double yMin, double yMax) {
        setFillStyle(FillPattern.SOLID, BgiColor.BLUE);
        bar3d(0, 0, getMaxX(), getMaxY(), 0, false);
        rectangle(a, yMin, b, yMax, BgiColor.WHITE);
        line(a, 0, b, 0, BgiColor.LIGHT_GREEN);
        line(0, yMin, 0, yMax, BgiColor.LIGHT_GREEN);
        drawTicks(a, b, yMin, yMax);
    }

    /** Marcas de división en ambos ejes (equivale a {@code Divisiones}). */
    private void drawTicks(double a, double b, double yMin, double yMax) {
        double deltaX = (b - a) / DIVISIONS;
        double deltaY = (yMax - yMin) / DIVISIONS;
        double tickHeight = (yMax - yMin) / TICK_RATIO;
        for (double x = a; x <= b; x += deltaX) {
            line(x, 0, x, tickHeight, BgiColor.WHITE);
        }
        double tickWidth = (b - a) / TICK_RATIO;
        for (double y = yMin; y <= yMax; y += deltaY) {
            line(0, y, tickWidth, y, BgiColor.WHITE);
        }
    }

    /**
     * Valores numéricos de las divisiones (equivale a {@code Textos}).
     * <p>
     * El original escribía cada número con su esquina en la marca, así que
     * las marcas del eje Y tachaban los números y el eje Y cruzaba el 0.
     * Aquí los números del eje X van debajo del eje, centrados en su marca,
     * los del eje Y a la derecha de la marca y centrados verticalmente, y el
     * 0 se escribe una sola vez junto al origen. Ningún número se sale de la
     * pantalla.
     */
    private void drawAxisLabels(double a, double b, double yMin, double yMax) {
        double deltaX = (b - a) / DIVISIONS;
        double deltaY = (yMax - yMin) / DIVISIONS;
        Point tickEnd = toScreen((b - a) / TICK_RATIO, 0);
        Point origin = toScreen(0, 0);

        setTextStyle(FontName.DEFAULT, TextDirection.HORIZONTAL, 1);
        setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        setColor(BgiColor.WHITE);
        int labelHeight = textHeight("0");

        for (double x = a; x <= b; x += deltaX) {
            String text = formatNumber(x);
            int labelWidth = textWidth(text);
            Point tick = toScreen(x, 0);
            int left = isZero(x) ? origin.x() + LABEL_GAP : tick.x() - labelWidth / 2;
            outTextXY(clamp(left, getMaxX() - labelWidth), tick.y() + LABEL_GAP, text);
        }
        for (double y = yMin; y <= yMax; y += deltaY) {
            if (isZero(y)) {
                continue;
            }
            Point tick = toScreen(0, y);
            int top = clamp(tick.y() - labelHeight / 2, getMaxY() - labelHeight);
            outTextXY(tickEnd.x() + LABEL_GAP, top, formatNumber(y));
        }
    }

    /** Ajusta una coordenada para que el texto no se salga de la pantalla. */
    private static int clamp(int position, int maxPosition) {
        return Math.max(LABEL_GAP, Math.min(maxPosition - LABEL_GAP, position));
    }

    private static boolean isZero(double value) {
        return Math.abs(value) < 1e-9;
    }

    /** Número con 3 dígitos significativos, sin ceros sobrantes (como {@code gcvt}). */
    public static String formatNumber(double value) {
        if (isZero(value)) {
            return "0";
        }
        return BigDecimal.valueOf(value).round(LABEL_PRECISION).stripTrailingZeros().toPlainString();
    }
}
