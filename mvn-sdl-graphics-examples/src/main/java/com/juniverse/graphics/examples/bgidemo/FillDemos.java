package com.juniverse.graphics.examples.bgidemo;

import com.juniverse.graphics.dto.Point;
import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.text.VerticalAlignment;

import java.util.ArrayList;
import java.util.List;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Demostraciones de relleno: {@code FillStyleDemo}, {@code FillPatternDemo}
 * y {@code PolyDemo}.
 */
public final class FillDemos {

    /** Patrones de usuario de FillPatternDemo (8 filas de 8 bits). */
    private static final int[][] USER_PATTERNS = {
            {0xAA, 0x55, 0xAA, 0x55, 0xAA, 0x55, 0xAA, 0x55},
            {0x33, 0x33, 0xCC, 0xCC, 0x33, 0x33, 0xCC, 0xCC},
            {0xF0, 0xF0, 0xF0, 0xF0, 0x0F, 0x0F, 0x0F, 0x0F},
            {0x00, 0x10, 0x28, 0x44, 0x28, 0x10, 0x00, 0x00},
            {0x00, 0x70, 0x20, 0x27, 0x24, 0x24, 0x07, 0x00},
            {0x00, 0x00, 0x00, 0x18, 0x18, 0x00, 0x00, 0x00},
            {0x00, 0x00, 0x3C, 0x3C, 0x3C, 0x3C, 0x00, 0x00},
            {0x00, 0x7E, 0x7E, 0x7E, 0x7E, 0x7E, 0x7E, 0x00},
            {0x00, 0x00, 0x22, 0x08, 0x00, 0x22, 0x1C, 0x00},
            {0xFF, 0x7E, 0x3C, 0x18, 0x18, 0x3C, 0x7E, 0xFF},
            {0x00, 0x10, 0x10, 0x7C, 0x10, 0x10, 0x00, 0x00},
            {0x00, 0x42, 0x24, 0x18, 0x18, 0x24, 0x42, 0x00}
    };

    private static final int POLYGON_POINTS = 6;

    private final DemoFrame frame;

    public FillDemos(DemoFrame frame) {
        this.frame = frame;
    }

    /** Muestra los 12 estilos de relleno predefinidos (FillStyleDemo). */
    public void fillStyleDemo() {
        frame.mainWindow("Pre-defined Fill Styles");
        drawBoxGrid(style -> setFillStyle(FillPattern.fromCode(style), frame.maxColors() - 1));
        frame.pause();
    }

    /** Muestra 12 patrones de relleno definidos por el usuario (FillPatternDemo). */
    public void fillPatternDemo() {
        frame.mainWindow("User Defined Fill Styles");
        drawBoxGrid(style -> setFillPattern(USER_PATTERNS[style], frame.maxColors() - 1));
        frame.pause();
    }

    /** Polígonos rellenos al azar hasta que se presione una tecla (PolyDemo). */
    public  void polyDemo() {
        frame.mainWindow("DrawPoly / FillPoly Demonstration");
        frame.statusLine("ESC Aborts - Press a Key to stop");

        while (!kbhit()) {
            int color = 1 + frame.random(frame.maxColors() - 1);
            setFillStyle(FillPattern.fromCode(frame.random(10)), color);
            setColor(color);

            List<Point> polygon = new ArrayList<>();
            for (int i = 0; i < POLYGON_POINTS - 1; i++) {
                polygon.add(new Point(frame.random(frame.maxX()), frame.random(frame.maxY())));
            }
            // Igual que el original: el último punto toma la x del primero y la y del segundo.
            polygon.add(new Point(polygon.get(0).x(), polygon.get(1).y()));
            fillPoly(polygon);
        }
        frame.pause();
    }

    /** Cuadrícula de 3x4 cajas rellenas con el estilo número 0..11. */
    private static void drawBoxGrid(java.util.function.IntConsumer applyStyle) {
        Viewport vp = getViewSettings();
        int w = 2 * ((vp.right() + 1) / 13);
        int h = 2 * ((vp.bottom() - 10) / 10);
        int x = w / 2;
        int y = h / 2;
        int style = 0;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 4; column++) {
                applyStyle.accept(style);
                bar(x, y, x + w, y + h);
                rectangle(x, y, x + w, y + h);
                outTextXY(x + w / 2, y + h + 4, String.valueOf(style));
                style++;
                x += (w / 2) * 3;
            }
            x = w / 2;
            y += (h / 2) * 3;
        }
        setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
    }
}
