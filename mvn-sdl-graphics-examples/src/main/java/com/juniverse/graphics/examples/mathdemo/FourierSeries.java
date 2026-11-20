package com.juniverse.graphics.examples.mathdemo;

import com.juniverse.graphics.color.BgiColor;
import com.juniverse.graphics.image.BgiImage;
import com.juniverse.graphics.platform.ScalingMode;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.style.RasterOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Port of {@code FOURIER.C}: builds a square wave by summing the
 * harmonics of its Fourier series.
 * <pre>
 *   y = sin(wt) + (1/3) sin(3wt) + (1/5) sin(5wt) + (1/7) sin(7wt) + ...
 * </pre>
 * The more terms are summed, the more the result resembles a
 * square wave. The program asks for the number of terms, draws the wave and waits for
 * a key (ESC ends).
 * <p>
 * The helper functions of the original are replaced as follows:
 * {@code gprintfxy} → {@link #printAt}, {@code gscanfxy} → {@link #readInt}
 * and {@code ggets} → {@link #readLine}.
 * <p>
 * Run with:
 * {@code mvn compile exec:java -Dexec.mainClass=com.juniverse.graphics.examples.mathdemo.FourierSeries}
 */
public final class FourierSeries {

    private static final Logger LOG = LoggerFactory.getLogger(FourierSeries.class);

    private static final int ESC = 0x1B;
    private static final int ENTER = 13;
    private static final int BACKSPACE = 8;

    /** Checkerboard pattern used as background. */
    private static final int[] BACKGROUND_PATTERN = {0xAA, 0x55, 0xAA, 0x55, 0xAA, 0x55, 0xAA, 0x55};

    /** Origin of the graph and wave scale (in pixels). */
    private static final int ORIGIN_X = 100;
    private static final int ORIGIN_Y = 200;
    private static final int SAMPLES = 400;
    private static final double AMPLITUDE = 185.0;

    private static final Pattern LEADING_INTEGER = Pattern.compile("^\\s*([+-]?\\d+)");

    private FourierSeries() {
    }

    public static void main(String[] args) {
        int gdriver = DETECT, gmode = 0;
        initGraph(gdriver, gmode, "");
        int errorCode = graphResult();
        if (errorCode != GR_OK) {
            LOG.atError()
                    .setMessage("Graphics error: {}")
                    .addArgument(() -> graphErrorMsg(errorCode))
                    .log();
            return;
        }
        try {
            setWindowTitle("SDL Graphics - Fourier series (FOURIER.C)");
            setWindowScaling(ScalingMode.KEEP_ASPECT);
            run();
        } finally {
            closeGraph();
        }
    }

    /**
     * Asks for the number of terms and draws the wave until ESC is pressed.
     * The {@code exit()} calls of the original are replaced by {@code return} so
     * that the window is always closed in {@link #main}.
     */
    private static void run() {
        int maxX = getMaxX();
        int maxY = getMaxY();

        while (true) {
            drawBackground(maxX, maxY);
            printAt(220, maxY / 3 + 45, "Number of terms -> ");
            int terms = readInt(430, maxY / 3 + 45);
            LOG.debug("Number of terms: {}", terms);

            if (terms <= 0) {
                outTextXY(220, maxY / 3 + 80, "Error: the number must be greater than zero.");
                LOG.warn("Invalid number of terms: {}", terms);
                getch();
                return;
            }

            drawBackground(maxX, maxY);
            line(ORIGIN_X, 50, ORIGIN_X, 350);
            line(ORIGIN_X, ORIGIN_Y, ORIGIN_X + SAMPLES, ORIGIN_Y);
            drawSquareWave(terms);

            setFillStyle(FillPattern.INTERLEAVE, BgiColor.LIGHT_BLUE);
            floodFill(maxX / 2 - 100, maxY / 3, getMaxColor());
            floodFill(maxX / 2 + 100, maxY / 2, getMaxColor());

            setColor(BgiColor.WHITE);
            printAt(300, 50, " <- %2d Harmonics", terms);
            outTextXY(maxX / 2 - 56, 15, "Fourier Series");
            printAt(maxX - 180, maxY - 20, "Terms: %d", terms);
            if (pause()) {
                return;
            }
        }
    }

    /** Clears the screen, fills it with the blue pattern and draws the double frame. */
    private static void drawBackground(int maxX, int maxY) {
        clearDevice();
        setFillPattern(BACKGROUND_PATTERN, BgiColor.BLUE.code());
        floodFill(0, 0, getMaxColor());
        rectangle(10, 10, maxX - 10, maxY - 10);
        rectangle(0, 0, maxX, maxY);
    }

    /** Draws the sum of the first {@code terms} odd harmonics. */
    private static void drawSquareWave(int terms) {
        moveTo(ORIGIN_X, ORIGIN_Y);
        for (int i = 0; i <= SAMPLES; i++) {
            double angle = i * 2.0 * Math.PI / SAMPLES;
            double y = 0.0;
            for (int j = 1; j <= terms; j++) {
                double harmonic = 2.0 * j - 1.0;
                y += (AMPLITUDE / harmonic) * Math.sin(harmonic * angle);
            }
            lineTo(ORIGIN_X + i, ORIGIN_Y - (int) y);
        }
    }

    /**
     * Shows the notice and waits for a key (equivalent to Pausa()).
     *
     * @return true if ESC was pressed to exit
     */
    private static boolean pause() {
        setColor(BgiColor.WHITE);
        moveTo(20, getMaxY() - 20);
        outText("ESC to exit...");
        return getch() == ESC;
    }

    /** Writes formatted text at (x, y) (equivalent to gprintfxy). */
    private static void printAt(int x, int y, String format, Object... arguments) {
        outTextXY(x, y, String.format(format, arguments));
    }

    /**
     * Reads an integer typed in graphics mode at (x, y) (equivalent to
     * gscanfxy with "%i"). If the text does not start with a number it returns 0.
     */
    private static int readInt(int x, int y) {
        Matcher matcher = LEADING_INTEGER.matcher(readLine(x, y));
        if (!matcher.find()) {
            return 0;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Reads a line of text in graphics mode (equivalent to ggets): shows each
     * typed character, allows deleting with Backspace and ends with Enter.
     * <p>
     * Before writing each character the portion of the screen it
     * is going to cover is saved; this way, when it is deleted the exact background is restored (the
     * blue pattern) instead of painting a black hole.
     */
    private static String readLine(int x, int y) {
        int charWidth = textWidth("H");
        int charHeight = textHeight("H");
        int maxChars = (getViewSettings().right() - x) / charWidth - 1;
        if (maxChars <= 0) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        Deque<BgiImage> coveredBackground = new ArrayDeque<>();
        moveTo(x, y);
        toggleCursor(charWidth, charHeight);
        while (true) {
            int key = getch();
            if (key == ENTER) {
                break;
            }
            if (key == 0) {
                getch(); // Extended key (arrows, F1...): ignored.
                continue;
            }
            toggleCursor(charWidth, charHeight);
            if (key == BACKSPACE) {
                if (!text.isEmpty()) {
                    text.deleteCharAt(text.length() - 1);
                    moveRel(-charWidth, 0);
                    putImage(getX(), getY(), coveredBackground.pop(), RasterOperation.COPY);
                }
            } else if (key >= ' ' && text.length() < maxChars) {
                coveredBackground.push(getImage(getX(), getY(), getX() + charWidth - 1, getY() + charHeight - 1));
                outText(String.valueOf((char) key));
                text.append((char) key);
            }
            toggleCursor(charWidth, charHeight);
        }
        toggleCursor(charWidth, charHeight);
        return text.toString();
    }

    /** Draws or erases (with XOR) the cursor under the current position. */
    private static void toggleCursor(int charWidth, int charHeight) {
        setWriteMode(RasterOperation.XOR);
        line(getX(), getY() + charHeight, getX() + charWidth - 1, getY() + charHeight);
        setWriteMode(RasterOperation.COPY);
    }
}
