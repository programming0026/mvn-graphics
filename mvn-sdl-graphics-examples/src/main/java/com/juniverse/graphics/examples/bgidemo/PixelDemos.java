package com.juniverse.graphics.examples.bgidemo;

import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.image.BgiImage;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.style.RasterOperation;

import java.util.Random;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Demostraciones de píxeles e imágenes: {@code PutPixelDemo} y
 * {@code PutImageDemo}.
 */
public final class PixelDemos {

    private static final int PIXEL_SEED = 1958;
    private static final int PIXEL_COUNT = 5000;
    /**
     * En una PC de la época, poner y quitar 5000 puntos tardaba varios
     * segundos y se veía el proceso. Hoy tarda menos de un cuadro de pantalla,
     * así que se dibuja en tandas con una pequeña pausa para que se aprecie.
     */
    private static final int PIXELS_PER_STEP = 50;
    private static final int STEP_DELAY_MILLIS = 20;
    /** Tiempo que se muestra el campo completo de puntos antes de borrarlo. */
    private static final int FULL_FIELD_MILLIS = 1500;

    private final DemoFrame frame;

    public PixelDemos(DemoFrame frame) {
        this.frame = frame;
    }

    /**
     * Pone 5000 puntos al azar y luego los borra repitiendo la misma secuencia
     * aleatoria (PutPixelDemo).
     */
    public void putPixelDemo() {
        frame.mainWindow("PutPixel / GetPixel Demonstration");
        Viewport vp = getViewSettings();
        int height = vp.bottom() - vp.top();
        int width = vp.right() - vp.left();

        Random sequence = new Random(PIXEL_SEED);
        for (int i = 0; i < PIXEL_COUNT; i++) {
            int x = 1 + sequence.nextInt(width - 1);
            int y = 1 + sequence.nextInt(height - 1);
            putPixel(x, y, sequence.nextInt(frame.maxColors()));
            paceDrawing(i);
        }
        delay(FULL_FIELD_MILLIS);

        sequence = new Random(PIXEL_SEED);
        for (int i = 0; i < PIXEL_COUNT; i++) {
            int x = 1 + sequence.nextInt(width - 1);
            int y = 1 + sequence.nextInt(height - 1);
            int color = getPixel(x, y);
            if (color == sequence.nextInt(frame.maxColors())) {
                putPixel(x, y, 0);
            }
            paceDrawing(i);
        }
        frame.pause();
    }

    /** Hace una pequeña pausa cada {@link #PIXELS_PER_STEP} puntos para que se vea el avance. */
    private static void paceDrawing(int index) {
        if ((index + 1) % PIXELS_PER_STEP == 0) {
            delay(STEP_DELAY_MILLIS);
        }
    }

    /** Un platillo volador que se mueve entre estrellas con getImage/putImage XOR (PutImageDemo). */
    public void putImageDemo() {
        final int radius = 20;
        final int startX = 100;
        final int startY = 50;

        frame.mainWindow("GetImage / PutImage Demonstration");
        Viewport vp = getViewSettings();

        setFillStyle(FillPattern.SOLID, getMaxColor());
        fillEllipse(startX, startY, radius, (radius / 3) + 2);
        ellipse(startX, startY - 4, 190, 357, radius, radius / 3);
        line(startX + 7, startY - 6, startX + 10, startY - 12);
        circle(startX + 10, startY - 12, 2);
        line(startX - 7, startY - 6, startX - 10, startY - 12);
        circle(startX - 10, startY - 12, 2);

        int left = startX - (radius + 1);
        int top = startY - 14;
        int right = startX + (radius + 1);
        int bottom = startY + (radius / 3) + 3;
        int width = right - left + 1;
        int height = bottom - top + 1;
        BgiImage saucer = getImage(left, top, right, bottom);
        putImage(left, top, saucer, RasterOperation.XOR);

        for (int i = 0; i < 1000; i++) {
            putPixel(frame.random(frame.maxX()), frame.random(frame.maxY()),
                    frame.random(frame.maxColors() - 1) + 1);
        }

        int x = frame.maxX() / 2;
        int y = frame.maxY() / 2;
        int pauseTime = 70;
        while (!kbhit()) {
            putImage(x, y, saucer, RasterOperation.XOR); // dibuja
            delay(pauseTime);
            putImage(x, y, saucer, RasterOperation.XOR); // borra

            int step = frame.random(2 * radius);
            if ((step / 2) % 2 != 0) {
                step = -step;
            }
            x += step;
            step = frame.random(radius);
            if ((step / 2) % 2 != 0) {
                step = -step;
            }
            y += step;

            if (vp.left() + x + width - 1 > vp.right()) {
                x = vp.right() - vp.left() - width + 1;
            } else if (x < 0) {
                x = 0;
            }
            if (vp.top() + y + height - 1 > vp.bottom()) {
                y = vp.bottom() - vp.top() - height + 1;
            } else if (y < 0) {
                y = 0;
            }
        }
        frame.pause();
    }
}
