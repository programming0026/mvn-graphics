package com.juniverse.graphics.examples.bgidemo;

import com.juniverse.graphics.color.BgiColor;
import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.examples.dto.PaletteBox;
import com.juniverse.graphics.style.FillPattern;

import java.util.ArrayList;
import java.util.List;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Demostraciones de color: {@code ColorDemo} y {@code PaletteDemo}.
 */
public final class ColorDemos {

    private final DemoFrame frame;

    public ColorDemos(DemoFrame frame) {
        this.frame = frame;
    }

    /** Muestra la paleta actual en una cuadrícula de 3x5 cajas (ColorDemo). */
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
     * Llena la pantalla de cajas de colores y cambia la paleta al azar hasta
     * que se presione una tecla (PaletteDemo).
     * <p>
     * En una tarjeta EGA/VGA, cambiar una entrada de la paleta recoloreaba al
     * instante todos los píxeles de ese color. Aquí los píxeles guardan su
     * color real, así que tras cambiar una entrada se vuelven a pintar las
     * cajas que la usan.
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
     * Convierte un color de hardware EGA (6 bits: rgbRGB) a RGB de 8 bits,
     * que es lo que recibía {@code setpalette} en el original.
     */
    private static int[] egaToRgb(int ega) {
        int code = ega & 0x3F;
        int red = ((code >> 2) & 1) * 2 + ((code >> 5) & 1);
        int green = ((code >> 1) & 1) * 2 + ((code >> 4) & 1);
        int blue = (code & 1) * 2 + ((code >> 3) & 1);
        return new int[]{red * 85, green * 85, blue * 85};
    }

    /** Pinta una caja de PaletteDemo con su color de la paleta actual. */
    private static void paint(PaletteBox box) {
        setFillStyle(FillPattern.SOLID, box.color());
        bar(box.left(), box.top(), box.right(), box.bottom());
    }
}
