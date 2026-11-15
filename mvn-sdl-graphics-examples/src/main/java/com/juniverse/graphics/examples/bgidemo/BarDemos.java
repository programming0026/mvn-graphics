package com.juniverse.graphics.examples.bgidemo;

import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.text.FontName;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.text.TextDirection;
import com.juniverse.graphics.text.VerticalAlignment;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Demostraciones de barras: {@code Bar3DDemo}, {@code BarDemo} y
 * {@code RandomBars}.
 */
public final class BarDemos {

    private static final int[] BAR3D_HEIGHTS = {1, 3, 5, 4, 3, 2, 1, 5, 4, 2, 3};
    private static final int[] BAR_HEIGHTS = {1, 3, 5, 2, 4};
    private static final int[] BAR_STYLES = {1, 3, 10, 5, 9, 1};

    private final DemoFrame frame;

    public BarDemos(DemoFrame frame) {
        this.frame = frame;
    }

    /** Gráfica de barras tridimensionales (Bar3DDemo). */
    public void bar3dDemo() {
        frame.mainWindow("Bar 3-D / Rectangle Demonstration");

        int h = 3 * textHeight("H");
        Viewport vp = getViewSettings();
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        frame.changeTextStyle(FontName.TRIPLEX, TextDirection.HORIZONTAL, 4);
        outTextXY(frame.maxX() / 2, 6, "These are 3-D Bars");
        frame.changeTextStyle(FontName.DEFAULT, TextDirection.HORIZONTAL, 1);
        setViewport(vp.left() + 50, vp.top() + 40, vp.right() - 50, vp.bottom() - 10, true);
        vp = getViewSettings();
        int areaHeight = vp.bottom() - vp.top();
        int areaWidth = vp.right() - vp.left();

        line(h, h, h, areaHeight - h);
        line(h, areaHeight - h, areaWidth - h, areaHeight - h);
        int xStep = (areaWidth - 2 * h) / 10;
        int yStep = (areaHeight - 2 * h) / 5;
        drawYAxisLabels(h, areaHeight, yStep);

        int j = h;
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        for (int i = 0; i < 11; i++) {
            int color = frame.random(frame.maxColors());
            setFillStyle(FillPattern.fromCode(i + 1), color);
            line(j, areaHeight - h, j, (areaHeight - 3) - (h / 2));
            outTextXY(j, areaHeight - (h / 2), String.valueOf(i));
            if (i != 10) {
                int barBottom = areaHeight - h - 1;
                bar3d(j, (areaHeight - h) - (BAR3D_HEIGHTS[i] * yStep), j + xStep, barBottom, 15, true);
            }
            j += xStep;
        }
        frame.pause();
    }

    /** Gráfica de barras planas con bar y rectangle (BarDemo). */
    public void barDemo() {
        frame.mainWindow("Bar / Rectangle demonstration");

        int h = 3 * textHeight("H");
        Viewport vp = getViewSettings();
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        frame.changeTextStyle(FontName.TRIPLEX, TextDirection.HORIZONTAL, 4);
        outTextXY(frame.maxX() / 2, 6, "These are 2-D Bars");
        frame.changeTextStyle(FontName.DEFAULT, TextDirection.HORIZONTAL, 1);
        setViewport(vp.left() + 50, vp.top() + 30, vp.right() - 50, vp.bottom() - 10, true);

        vp = getViewSettings();
        int areaHeight = vp.bottom() - vp.top();
        int areaWidth = vp.right() - vp.left();

        line(h, h, h, areaHeight - h);
        line(h, areaHeight - h, areaHeight - h, areaHeight - h);
        int yStep = (areaHeight - 2 * h) / 5;
        int xStep = (areaWidth - 2 * h) / 5;
        drawYAxisLabels(h, areaHeight, yStep);

        int j = h;
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        for (int i = 0; i < 6; i++) {
            setFillStyle(FillPattern.fromCode(BAR_STYLES[i]), frame.random(frame.maxColors()));
            line(j, areaHeight - h, j, areaHeight - 3 - (h / 2));
            outTextXY(j, areaHeight - (h / 2), String.valueOf(i));
            if (i != 5) {
                bar(j, (areaHeight - h) - (BAR_HEIGHTS[i] * yStep), j + xStep, areaHeight - h - 1);
                rectangle(j, (areaHeight - h) - (BAR_HEIGHTS[i] * yStep), j + xStep, areaHeight - h);
            }
            j += xStep;
        }
        frame.pause();
    }

    /** Barras de colores y tamaños al azar hasta que se presione una tecla (RandomBars). */
    public void randomBars() {
        frame.mainWindow("Random Bars");
        frame.statusLine("Esc aborts or press a key...");
        while (!kbhit()) {
            int color = frame.random(frame.maxColors() - 1) + 1;
            setColor(color);
            setFillStyle(FillPattern.fromCode(frame.random(11) + 1), color);
            bar3d(frame.random(getMaxX()), frame.random(getMaxY()),
                    frame.random(getMaxX()), frame.random(getMaxY()), 0, false);
        }
        frame.pause();
    }

    /** Marcas y números 0..5 del eje vertical. */
    private static void drawYAxisLabels(int h, int areaHeight, int yStep) {
        int j = areaHeight - h;
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        for (int i = 0; i < 6; i++) {
            line(h / 2, j, h, j);
            outTextXY(0, j, String.valueOf(i));
            j -= yStep;
        }
    }
}
