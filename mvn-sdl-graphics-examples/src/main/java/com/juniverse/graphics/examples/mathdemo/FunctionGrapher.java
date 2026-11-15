package com.juniverse.graphics.examples.mathdemo;

import com.juniverse.graphics.color.BgiColor;
import com.juniverse.graphics.platform.ScalingMode;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.text.FontName;
import com.juniverse.graphics.text.TextDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.function.DoubleUnaryOperator;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Graficadora de funciones.
 * <p>
 * Primero muestra una tabla con los valores de x y f(x) de -20 a 20 (en
 * pasos de 0.25) y, al presionar una tecla, grafica la función con
 * {@link Plot2D}. La función del original es {@code f(x) = x²}; para graficar
 * otra basta con cambiar {@link #FUNCTION}.
 * <p>
 */
public final class FunctionGrapher {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionGrapher.class);

    /** Función a graficar (equivale a {@code float f(float)}). */
    private static final DoubleUnaryOperator FUNCTION = x -> x * x * x;
    private static final String FUNCTION_NAME = "x³";

    /**
     * Posición lógica del título: esquina superior izquierda, lejos de los
     * ejes y de sus números (en el original estaba en (5, 1), encima del eje X).
     */
    private static final double TITLE_X = -19.0;
    private static final double TITLE_Y = 19.0;

    private static final double FROM = -20.0;
    private static final double TO = 20.0;
    private static final double STEP = 0.25;
    private static final int ROW_HEIGHT = 10;

    private FunctionGrapher() {
    }

    public static void main(String[] args) {
        initGraph(DETECT, 0, "");
        if (graphResult() != GR_OK) {
            LOG.error("Graphics initialization failed: no graphics card detected, cannot plot");
            return;
        }
        try {
            setWindowTitle("SDL Graphics - Function grapher (GRAFICA.C)");
            setWindowScaling(ScalingMode.KEEP_ASPECT);

            showTable();
            getch();

            clearDevice();
            Plot2D plot = new Plot2D();
            plot.drawCoordinates(FROM, TO, FROM, TO);
            plot.title(TITLE_X, TITLE_Y, "Plotting: " + FUNCTION_NAME, BgiColor.YELLOW);
            for (double x = FROM; x <= TO; x += STEP) {
                plot.plot(x, FUNCTION.applyAsDouble(x), BgiColor.WHITE);
            }
            plot.close();
        } finally {
            // Si algo falla antes de plot.close(), la ventana también se cierra.
            closeGraph();
        }
    }

    /**
     * Tabula la función. Igual que en el original, la tabla tiene 161 filas y
     * solo caben en pantalla las primeras.
     */
    private static void showTable() {
        clearDevice();
        setFillStyle(FillPattern.SOLID, BgiColor.BLUE);
        bar3d(0, 0, getMaxX(), getMaxY(), 0, false);
        setTextStyle(FontName.SMALL, TextDirection.HORIZONTAL, 2);
        setColor(BgiColor.YELLOW);
        outTextXY(250, 0, "X");
        outTextXY(350, 0, "Y");
        outTextXY(420, 465, "Press a key to continue...");
        outTextXY(60, getMaxY() / 2, "Table -> ");

        setColor(BgiColor.LIGHT_GREEN);
        int row = ROW_HEIGHT;
        for (double x = FROM; x <= TO; x += STEP, row += ROW_HEIGHT) {
            outTextXY(230, row, String.format(Locale.ROOT, "%1.3f", x));
            outTextXY(330, row, String.format(Locale.ROOT, "%1.3f", FUNCTION.applyAsDouble(x)));
        }
    }
}
