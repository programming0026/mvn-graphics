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
 * Function grapher.
 * <p>
 * First it shows a table with the values of x and f(x) from -20 to 20 (in
 * steps of 0.25) and, when a key is pressed, graphs the function with
 * {@link Plot2D}. The function of the original is {@code f(x) = x²}; to graph
 * another one it is enough to change {@link #FUNCTION}.
 * <p>
 */
public final class FunctionGrapher {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionGrapher.class);

    /** Function to graph (equivalent to {@code float f(float)}). */
    private static final DoubleUnaryOperator FUNCTION = x -> x * x * x;
    private static final String FUNCTION_NAME = "x³";

    /**
     * Logical position of the title: top-left corner, away from the
     * axes and their numbers (in the original it was at (5, 1), above the X axis).
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
            // If something fails before plot.close(), the window is also closed.
            closeGraph();
        }
    }

    /**
     * Tabulates the function. Same as in the original, the table has 161 rows and
     * only the first ones fit on screen.
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
