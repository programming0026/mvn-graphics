package com.juniverse.graphics.examples.bgidemo;

import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.image.BgiImage;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.style.RasterOperation;

import java.util.Random;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Pixel and image demonstrations: {@code PutPixelDemo} and
 * {@code PutImageDemo}.
 */
public final class PixelDemos {

    private static final int PIXEL_SEED = 1958;
    private static final int PIXEL_COUNT = 5000;
    /**
     * On a PC of that era, putting and removing 5000 points took several
     * seconds and the process was visible. Today it takes less than one screen frame,
     * so it is drawn in batches with a small pause so that it can be appreciated.
     */
    private static final int PIXELS_PER_STEP = 50;
    private static final int STEP_DELAY_MILLIS = 20;
    /** Time the complete field of points is shown before erasing it. */
    private static final int FULL_FIELD_MILLIS = 1500;

    private final DemoFrame frame;

    public PixelDemos(DemoFrame frame) {
        this.frame = frame;
    }

    /**
     * Puts 5000 random points and then erases them by repeating the same
     * random sequence (PutPixelDemo).
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

    /** Makes a small pause every {@link #PIXELS_PER_STEP} points so the progress is visible. */
    private static void paceDrawing(int index) {
        if ((index + 1) % PIXELS_PER_STEP == 0) {
            delay(STEP_DELAY_MILLIS);
        }
    }

    /** A flying saucer moving between stars with getImage/putImage XOR (PutImageDemo). */
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
            putImage(x, y, saucer, RasterOperation.XOR); // draws
            delay(pauseTime);
            putImage(x, y, saucer, RasterOperation.XOR); // erases

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
