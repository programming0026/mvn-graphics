package com.juniverse.graphics.examples.bgidemo;

import com.juniverse.graphics.color.BgiColor;
import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.examples.dto.PaletteBox;
import com.juniverse.graphics.style.FillPattern;

import java.util.ArrayList;
import java.util.List;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Color demonstrations: {@code ColorDemo} and {@code PaletteDemo}.
 */
public final class ColorDemos {

    private final DemoFrame frame;

    public ColorDemos(DemoFrame frame) {
        this.frame = frame;
    }

    /** Shows the current palette in a 3x5 grid of boxes (ColorDemo). */
    public void colorDemo() {
        frame.mainWindow("Color Demonstration");

        int color = 1;
        Viewport vp = getViewSettings();
        int width = 2 * ((vp.right() + 1) / 16);
        int height = 2 * ((vp.bottom() - 10) / 10);
        int x = width / 2;
        int y = height / 2;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 5; column++) {
                setFillStyle(FillPattern.SOLID, color);
                setColor(color);
                bar(x, y, x + width, y + height);
                rectangle(x, y, x + width, y + height);
                if (color == BgiColor.BLACK.code()) {
                    setColor(BgiColor.WHITE);
                    rectangle(x, y, x + width, y + height);
                }
                outTextXY(x + width / 2, y + height + 4, String.valueOf(color));
                color = (color + 1) % frame.maxColors();
                x += (width / 2) * 3;
            }
            y += (height / 2) * 3;
            x = width / 2;
        }
        frame.pause();
    }

    /**
     * Fills the screen with colored boxes and changes the palette randomly until
     * a key is pressed (PaletteDemo).
     * <p>
     * On an EGA/VGA card, changing a palette entry instantly recolored
     * all the pixels of that color. Here pixels store their
     * real color, so after changing an entry the boxes that use it
     * are repainted.
     */
    public void paletteDemo() {
        frame.mainWindow("Palette Demonstration");
        frame.statusLine("Press any key to continue, ESC to Abort");

        Viewport vp = getViewSettings();
        int width = (vp.right() - vp.left()) / 15;
        int height = (vp.bottom() - vp.top()) / 10;
        List<PaletteBox> boxes = new ArrayList<>();

        int x = 0;
        int y = 0;
        int color = 1;
        for (int row = 0; row < 10; row++) {
            for (int column = 0; column < 15; column++) {
                PaletteBox box = new PaletteBox(x, y, x + width, y + height, color++);
                paint(box);
                boxes.add(box);
                x += width + 1;
                color = 1 + (color % (frame.maxColors() - 2));
            }
            x = 0;
            y += height + 1;
        }

        while (!kbhit()) {
            int entry = 1 + frame.random(frame.maxColors() - 2);
            int[] rgb = egaToRgb(frame.random(65));
            setRgbPalette(entry, rgb[0], rgb[1], rgb[2]);
            boxes.stream().filter(box -> box.color() == entry).forEach(ColorDemos::paint);
        }

        setAllPalette(frame.originalPalette());
        frame.pause();
    }

    /**
     * Converts an EGA hardware color (6 bits: rgbRGB) to 8-bit RGB,
     * which is what {@code setpalette} received in the original.
     */
    private static int[] egaToRgb(int ega) {
        int code = ega & 0x3F;
        int red = ((code >> 2) & 1) * 2 + ((code >> 5) & 1);
        int green = ((code >> 1) & 1) * 2 + ((code >> 4) & 1);
        int blue = (code & 1) * 2 + ((code >> 3) & 1);
        return new int[]{red * 85, green * 85, blue * 85};
    }

    /** Paints a PaletteDemo box with its color from the current palette. */
    private static void paint(PaletteBox box) {
        setFillStyle(FillPattern.SOLID, box.color());
        bar(box.left(), box.top(), box.right(), box.bottom());
    }
}
