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
 * Port of {@code GRAFICA.H}: small library to graph functions in
 * logical coordinates (those of the mathematical problem) on the screen.
 * <p>
 * The global variables of the original ({@code W1..W4}, {@code V1..V4},
 * {@code U1}, {@code U2}) become the state of an instance: the
 * "logical window" that is transformed into screen coordinates.
 * <p>
 * Equivalences: {@code VentanaLogica} → {@link #setLogicalWindow},
 * {@code Transforma} → {@link #toScreen}, {@code CoordenadasXY} →
 * {@link #drawCoordinates}, {@code Plot2D} → {@link #plot}, etc.
 */
public final class Plot2D {

    /** Number of divisions of each axis. */
    private static final double DIVISIONS = 10.0;
    /** The axis ticks measure 1/50 of the range. */
    private static final double TICK_RATIO = 50.0;
    /** Significant digits of the labels (like {@code gcvt(n, 3, ...)}). */
    private static final MathContext LABEL_PRECISION = new MathContext(3);
    /** Separation in pixels between an axis tick and its number. */
    private static final int LABEL_GAP = 3;

    // Logical window.
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    // Screen window and scale factors.
    private int screenLeft;
    private int screenRight;
    private int screenTop;
    private int screenBottom;
    private double scaleX;
    private double scaleY;

    /** Equivalent to {@code ClosePlot2D}: waits for a key and closes the graphics window. */
    public void close() {
        getch();
        closeGraph();
    }

    /**
     * Prepares the logical window, draws the axes and writes their values
     * (equivalent to {@code CoordenadasXY}).
     */
    public void drawCoordinates(double a, double b, double yMin, double yMax) {
        setLogicalWindow(a, yMin, b, yMax);
        drawAxes(a, b, yMin, yMax);
        drawAxisLabels(a, b, yMin, yMax);
    }

    /**
     * Defines the logical rectangle that occupies the whole screen (equivalent to
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

    /** Converts a logical point to screen coordinates (equivalent to {@code Transforma}). */
    public Point toScreen(double x, double y) {
        int screenX = (int) ((x - minX) * scaleX);
        int screenY = (int) ((screenBottom - screenTop) - (y - minY) * scaleY);
        return new Point(screenX, screenY);
    }

    /**
     * Draws a point of the graph joined to the previous one (equivalent to
     * {@code Plot2D}). Points outside the screen are ignored.
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

    /** Draws a segment in logical coordinates (equivalent to {@code Line}). */
    public void line(double x0, double y0, double x1, double y1, BgiColor color) {
        setColor(color);
        Point start = toScreen(x0, y0);
        Point end = toScreen(x1, y1);
        com.juniverse.graphics.api.Bgi.line(start.x(), start.y(), end.x(), end.y());
    }

    /** Draws a rectangle in logical coordinates (equivalent to {@code Rectangle}). */
    public void rectangle(double x1, double y1, double x2, double y2, BgiColor color) {
        setColor(color);
        Point corner1 = toScreen(x1, y1);
        Point corner2 = toScreen(x2, y2);
        com.juniverse.graphics.api.Bgi.rectangle(corner1.x(), corner1.y(), corner2.x(), corner2.y());
    }

    /** Writes a text at a logical position (equivalent to {@code Letrero}). */
    public void label(double x, double y, String message, BgiColor color, TextDirection direction) {
        setTextStyle(FontName.DEFAULT, direction, 1);
        setColor(color);
        Point point = toScreen(x, y);
        outTextXY(point.x(), point.y(), message);
    }

    /**
     * Writes a title at a logical position (equivalent to {@code Title}).
     * <p>
     * The original computed the screen position but wrote with the
     * logical coordinates ({@code outtextxy(x, y, ...)}), so the
     * title ended up in the top-left corner. Here the
     * transformed position is used, which was the intention.
     */
    public void title(double x, double y, String message, BgiColor color) {
        setColor(color);
        Point point = toScreen(x, y);
        setTextStyle(FontName.SMALL, TextDirection.HORIZONTAL, 4);
        setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        outTextXY(point.x(), point.y(), message);
    }

    /** Blue background, frame, axes and ticks (equivalent to {@code Ejes}). */
    private void drawAxes(double a, double b, double yMin, double yMax) {
        setFillStyle(FillPattern.SOLID, BgiColor.BLUE);
        bar3d(0, 0, getMaxX(), getMaxY(), 0, false);
        rectangle(a, yMin, b, yMax, BgiColor.WHITE);
        line(a, 0, b, 0, BgiColor.LIGHT_GREEN);
        line(0, yMin, 0, yMax, BgiColor.LIGHT_GREEN);
        drawTicks(a, b, yMin, yMax);
    }

    /** Division ticks on both axes (equivalent to {@code Divisiones}). */
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
     * Numeric values of the divisions (equivalent to {@code Textos}).
     * <p>
     * The original wrote each number with its corner at the tick, so
     * the Y axis ticks struck through the numbers and the Y axis crossed the 0.
     * Here the X axis numbers go below the axis, centered on their tick,
     * the Y axis ones to the right of the tick and vertically centered, and the
     * 0 is written only once next to the origin. No number goes off the
     * screen.
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

    /** Adjusts a coordinate so that the text does not go off the screen. */
    private static int clamp(int position, int maxPosition) {
        return Math.max(LABEL_GAP, Math.min(maxPosition - LABEL_GAP, position));
    }

    private static boolean isZero(double value) {
        return Math.abs(value) < 1e-9;
    }

    /** Number with 3 significant digits, without trailing zeros (like {@code gcvt}). */
    public static String formatNumber(double value) {
        if (isZero(value)) {
            return "0";
        }
        return BigDecimal.valueOf(value).round(LABEL_PRECISION).stripTrailingZeros().toPlainString();
    }
}
