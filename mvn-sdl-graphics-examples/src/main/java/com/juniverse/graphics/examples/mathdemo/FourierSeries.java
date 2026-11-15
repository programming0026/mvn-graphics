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
 * Port de {@code FOURIER.C}: construye una onda cuadrada sumando los
 * armónicos de su serie de Fourier.
 * <pre>
 *   y = sin(wt) + (1/3) sin(3wt) + (1/5) sin(5wt) + (1/7) sin(7wt) + ...
 * </pre>
 * Cuantos más términos se sumen, más se parece el resultado a una onda
 * cuadrada. El programa pide el número de términos, dibuja la onda y espera
 * una tecla (ESC termina).
 * <p>
 * Las funciones auxiliares del original se reemplazan así:
 * {@code gprintfxy} → {@link #printAt}, {@code gscanfxy} → {@link #readInt}
 * y {@code ggets} → {@link #readLine}.
 * <p>
 * Ejecutar con:
 * {@code mvn compile exec:java -Dexec.mainClass=com.juniverse.graphics.examples.mathdemo.FourierSeries}
 */
public final class FourierSeries {

    private static final Logger LOG = LoggerFactory.getLogger(FourierSeries.class);

    private static final int ESC = 0x1B;
    private static final int ENTER = 13;
    private static final int BACKSPACE = 8;

    /** Patrón de tablero de ajedrez usado como fondo. */
    private static final int[] BACKGROUND_PATTERN = {0xAA, 0x55, 0xAA, 0x55, 0xAA, 0x55, 0xAA, 0x55};

    /** Origen de la gráfica y escala de la onda (en píxeles). */
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
     * Pide el número de términos y dibuja la onda hasta que se presione ESC.
     * Los {@code exit()} del original se reemplazan por {@code return} para
     * que la ventana siempre se cierre en {@link #main}.
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

    /** Borra la pantalla, la rellena con el patrón azul y dibuja el doble marco. */
    private static void drawBackground(int maxX, int maxY) {
        clearDevice();
        setFillPattern(BACKGROUND_PATTERN, BgiColor.BLUE.code());
        floodFill(0, 0, getMaxColor());
        rectangle(10, 10, maxX - 10, maxY - 10);
        rectangle(0, 0, maxX, maxY);
    }

    /** Dibuja la suma de los primeros {@code terms} armónicos impares. */
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
     * Muestra el aviso y espera una tecla (equivale a Pausa()).
     *
     * @return verdadero si se presionó ESC para salir
     */
    private static boolean pause() {
        setColor(BgiColor.WHITE);
        moveTo(20, getMaxY() - 20);
        outText("ESC to exit...");
        return getch() == ESC;
    }

    /** Escribe texto con formato en (x, y) (equivale a gprintfxy). */
    private static void printAt(int x, int y, String format, Object... arguments) {
        outTextXY(x, y, String.format(format, arguments));
    }

    /**
     * Lee un número entero escrito en modo gráfico en (x, y) (equivale a
     * gscanfxy con "%i"). Si el texto no empieza con un número devuelve 0.
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
     * Lee una línea de texto en modo gráfico (equivale a ggets): muestra cada
     * carácter escrito, permite borrar con Retroceso y termina con Enter.
     * <p>
     * Antes de escribir cada carácter se guarda la porción de pantalla que
     * va a cubrir; así, al borrarlo se restaura el fondo exacto (el patrón
     * azul) en lugar de pintar un hueco negro.
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
                getch(); // Tecla extendida (flechas, F1...): se ignora.
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

    /** Dibuja o borra (con XOR) el cursor bajo la posición actual. */
    private static void toggleCursor(int charWidth, int charHeight) {
        setWriteMode(RasterOperation.XOR);
        line(getX(), getY() + charHeight, getX() + charWidth - 1, getY() + charHeight);
        setWriteMode(RasterOperation.COPY);
    }
}
