package com.juniverse.graphics.examples.mathdemo;

import com.juniverse.graphics.color.BgiColor;
import com.juniverse.graphics.platform.ScalingMode;
import com.juniverse.graphics.style.FillPattern;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Simula un display de 7 segmentos que cuenta de 00 a 19.
 * <p>
 * Cada segmento es un rectángulo con borde rojo que se rellena con
 * {@code floodFill}: en rojo claro si está encendido y en negro si está
 * apagado. Las siete funciones {@code seg_a()} ... {@code seg_g()} del
 * original se reemplazan por la tabla {@link Segment}, y el {@code switch}
 * de {@code num()} por la tabla {@link #DIGITS}.
 * <p>
 */
public final class SevenSegmentDisplay {

    /** Color del contorno de los segmentos ({@code setcolor(4)}). */
    private static final BgiColor OUTLINE_COLOR = BgiColor.RED;
    /** Color de un segmento encendido ({@code setfillstyle(1, 12)}). */
    private static final BgiColor LIT_COLOR = BgiColor.LIGHT_RED;
    /** Color de un segmento apagado ({@code setfillstyle(1, 0)}). */
    private static final BgiColor OFF_COLOR = BgiColor.BLACK;

    private static final int TENS_X = 220;
    private static final int UNITS_X = 330;
    private static final int DIGIT_Y = 150;
    private static final int STEP_MILLIS = 1000;

    /**
     * Segmentos del display. Las coordenadas son relativas a la esquina del
     * dígito; el relleno empieza 2 píxeles dentro de la esquina superior
     * izquierda, igual que en el original.
     */
    private enum Segment {
        A(0, 0, 60, 15),
        B(65, 20, 80, 80),
        C(65, 105, 80, 165),
        D(0, 170, 60, 185),
        E(-20, 105, -5, 165),
        F(-20, 20, -5, 80),
        G(0, 85, 60, 100);

        private static final int SEED_OFFSET = 2;

        private final int left;
        private final int top;
        private final int right;
        private final int bottom;

        private Segment(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        /** Dibuja el contorno y lo rellena con el estilo de relleno actual. */
        public void draw(int originX, int originY) {
            rectangle(originX + left, originY + top, originX + right, originY + bottom);
            floodFill(originX + left + SEED_OFFSET, originY + top + SEED_OFFSET, OUTLINE_COLOR);
        }
    }

    /** Segmentos encendidos para cada dígito (equivale al switch de num()). */
    private static final List<Set<Segment>> DIGITS = List.of(
            EnumSet.of(Segment.A, Segment.B, Segment.C, Segment.D, Segment.E, Segment.F),            // 0
            EnumSet.of(Segment.B, Segment.C),                                                        // 1
            EnumSet.of(Segment.A, Segment.B, Segment.D, Segment.E, Segment.G),                       // 2
            EnumSet.of(Segment.A, Segment.B, Segment.C, Segment.D, Segment.G),                       // 3
            EnumSet.of(Segment.B, Segment.C, Segment.F, Segment.G),                                  // 4
            EnumSet.of(Segment.A, Segment.C, Segment.D, Segment.F, Segment.G),                       // 5
            EnumSet.of(Segment.A, Segment.C, Segment.D, Segment.E, Segment.F, Segment.G),            // 6
            EnumSet.of(Segment.A, Segment.B, Segment.C, Segment.G),                                  // 7
            EnumSet.allOf(Segment.class),                                                            // 8
            EnumSet.of(Segment.A, Segment.B, Segment.C, Segment.D, Segment.F, Segment.G));           // 9

    private SevenSegmentDisplay() {
    }

    public static void main(String[] args) {
        initGraph(DETECT, 0, "");
        try {
            setWindowTitle("SDL Graphics - Seven-segment display (DISPLAY.C)");
            setWindowScaling(ScalingMode.KEEP_ASPECT);

            setColor(OUTLINE_COLOR);
            for (int tens = 0; tens <= 1; tens++) {
                showDigit(TENS_X, DIGIT_Y, tens);
                for (int units = 0; units <= 9; units++) {
                    showDigit(UNITS_X, DIGIT_Y, units);
                    delay(STEP_MILLIS);
                }
            }
            setColor(BgiColor.WHITE);
            outTextXY(360, 470, "Press a key to exit...");
            getch();
        } finally {
            closeGraph();
        }
    }

    /** Muestra un dígito con su esquina en (x, y) (equivale a display()). */
    private static void showDigit(int x, int y, int digit) {
        setFillStyle(FillPattern.SOLID, OFF_COLOR);
        for (Segment segment : Segment.values()) {
            segment.draw(x, y);
        }
        setFillStyle(FillPattern.SOLID, LIT_COLOR);
        for (Segment segment : DIGITS.get(digit)) {
            segment.draw(x, y);
        }
    }
}
