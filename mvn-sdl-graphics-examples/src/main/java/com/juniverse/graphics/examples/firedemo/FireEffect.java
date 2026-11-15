package com.juniverse.graphics.examples.firedemo;

import com.juniverse.graphics.image.BgiImage;
import com.juniverse.graphics.platform.ScalingMode;
import com.juniverse.graphics.style.RasterOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Random;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Simulacion de fuego.
 * <p>
 * Aquí:
 * <ul>
 *     <li>la memoria de video es el arreglo de "calor" de {@link FireSimulation};</li>
 *     <li>la paleta VGA de 256 colores es {@link #buildPalette()}, en RGB;</li>
 *     <li>cada cuadro se convierte en una {@link BgiImage} y se dibuja con
 *     {@code putImage}.</li>
 * </ul>
 * <p>
 */
public final class FireEffect {

    private static final Logger LOG = LoggerFactory.getLogger(FireEffect.class);

    private static final int FRAME_DELAY_MILLIS = 20;

    private FireEffect() {
    }

    public static void main(String[] args) {
        initGraph(DETECT, 0, "");
        if (graphResult() != GR_OK) {
            LOG.error("Graphics initialization failed: no graphics card detected");
            return;
        }
        try {
            setWindowTitle("SDL Graphics - Fire effect (FIRE.C)");
            setWindowScaling(ScalingMode.KEEP_ASPECT);

            int[] palette = buildPalette();
            FireSimulation fire = new FireSimulation(new Random());
            while (!kbhit()) {
                fire.step();
                BgiImage frame = fire.render(palette).scaledTo(getMaxX() + 1, getMaxY() + 1);
                putImage(0, 0, frame, RasterOperation.COPY);
                delay(FRAME_DELAY_MILLIS);
            }
            getch();
        } finally {
            closeGraph();
        }
    }

    /**
     * Construye la paleta de 256 colores del original: negro → rojo →
     * amarillo → blanco. Los valores de la paleta VGA tienen 6 bits (0..63)
     * y se convierten a 8 bits. Las entradas que el original no programaba
     * (129..255) se dejan en blanco.
     */
    private static int[] buildPalette() {
        int[] palette = new int[256];
        Arrays.fill(palette, vgaColor(63, 63, 63));
        palette[0] = vgaColor(0, 0, 0);
        for (int x = 1; x <= 32; x++) {
            palette[x] = vgaColor(x * 2 - 1, 0, 0);
            palette[x + 32] = vgaColor(63, x * 2 - 1, 0);
            palette[x + 64] = vgaColor(63, 63, x * 2 - 1);
            palette[x + 96] = vgaColor(63, 63, 63);
        }
        return palette;
    }

    /** Convierte un color VGA de 6 bits por canal a un píxel ARGB opaco. */
    private static int vgaColor(int red, int green, int blue) {
        return 0xFF000000 | (red * 255 / 63) << 16 | (green * 255 / 63) << 8 | (blue * 255 / 63);
    }

    /**
     * Simulación del fuego: equivale a la memoria de video del modo 13h,
     * donde cada byte es un índice de la paleta ("calor").
     */
    public static final class FireSimulation {

        public static final int WIDTH = 640;
        public static final int HEIGHT = 480;

        /** Primera fila que se recalcula (las de arriba quedan negras). */
        private static final int FIRST_ROW = 60;
        /** Fila oculta (fuera de la pantalla) donde nacen las chispas. */
        private static final int SOURCE_ROW = HEIGHT;
        private static final int SPARK_HEAT = 160;

        private static final int LEFT_MARGIN = 2;
        private static final int STRIDE = WIDTH + 6;
        private static final int ROWS = HEIGHT + 4;

        private final int[] heat = new int[STRIDE * ROWS];
        private final Random random;

        public FireSimulation(Random random) {
            this.random = random;
        }

        /**
         * Avanza un cuadro, con el mismo algoritmo que el original: cada
         * bloque de 2x2 toma el promedio de sus vecinos de abajo menos 1, de
         * modo que el calor sube y se enfría; en la fila oculta inferior
         * aparecen chispas al azar.
         */
        public void step() {
            for (int x = 0; x <= WIDTH; x += 2) {
                int y = FIRST_ROW;
                do {
                    int c = (heat(x, y) + heat(x + 2, y) + heat(x - 2, y) + heat(x + 2, y + 2)) / 4;
                    if (c != 0) {
                        c--;
                    }
                    setBlock(x, y - 2, c);
                    y += 2;
                } while (y < SOURCE_ROW + 2);
                setHeat(x, SOURCE_ROW, random.nextInt(2) * SPARK_HEAT);
            }
        }

        /** Convierte el calor visible (320x200) en una imagen usando la paleta. */
        public BgiImage render(int[] palette) {
            int[] pixels = new int[WIDTH * HEIGHT];
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    pixels[y * WIDTH + x] = palette[Math.min(heat(x, y), palette.length - 1)];
                }
            }
            return new BgiImage(WIDTH, HEIGHT, pixels);
        }

        /** Escribe un bloque de 2x2 (equivale a los dos poke() del original). */
        private void setBlock(int x, int y, int value) {
            setHeat(x, y, value);
            setHeat(x + 1, y, value);
            setHeat(x, y + 1, value);
            setHeat(x + 1, y + 1, value);
        }

        private int heat(int x, int y) {
            return heat[y * STRIDE + x + LEFT_MARGIN];
        }

        private void setHeat(int x, int y, int value) {
            heat[y * STRIDE + x + LEFT_MARGIN] = value;
        }
    }
}
