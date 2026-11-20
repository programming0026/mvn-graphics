package com.juniverse.graphics.examples.bgidemo;

import com.juniverse.graphics.color.BgiColor;
import com.juniverse.graphics.dto.Point;
import com.juniverse.graphics.input.MouseEventKind;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.style.LineStyle;
import com.juniverse.graphics.style.RasterOperation;
import com.juniverse.graphics.text.FontName;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.text.TextDirection;
import com.juniverse.graphics.text.VerticalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.juniverse.graphics.api.Bgi.DETECT;
import static com.juniverse.graphics.api.Bgi.GR_OK;
import static com.juniverse.graphics.api.Bgi.NORM_WIDTH;
import static com.juniverse.graphics.api.Bgi.THICK_WIDTH;
import static com.juniverse.graphics.api.Bgi.VGA;
import static com.juniverse.graphics.api.Bgi.arc;
import static com.juniverse.graphics.api.Bgi.bar3d;
import static com.juniverse.graphics.api.Bgi.circle;
import static com.juniverse.graphics.api.Bgi.clearDevice;
import static com.juniverse.graphics.api.Bgi.clearResizeEvent;
import static com.juniverse.graphics.api.Bgi.closeGraph;
import static com.juniverse.graphics.api.Bgi.delay;
import static com.juniverse.graphics.api.Bgi.ellipse;
import static com.juniverse.graphics.api.Bgi.fillEllipse;
import static com.juniverse.graphics.api.Bgi.fillPoly;
import static com.juniverse.graphics.api.Bgi.floodFill;
import static com.juniverse.graphics.api.Bgi.getDriverName;
import static com.juniverse.graphics.api.Bgi.getGraphMode;
import static com.juniverse.graphics.api.Bgi.getMaxX;
import static com.juniverse.graphics.api.Bgi.getMaxY;
import static com.juniverse.graphics.api.Bgi.getModeName;
import static com.juniverse.graphics.api.Bgi.getMouseClick;
import static com.juniverse.graphics.api.Bgi.getch;
import static com.juniverse.graphics.api.Bgi.graphErrorMsg;
import static com.juniverse.graphics.api.Bgi.graphResult;
import static com.juniverse.graphics.api.Bgi.initGraph;
import static com.juniverse.graphics.api.Bgi.isMouseClick;
import static com.juniverse.graphics.api.Bgi.isResizeEvent;
import static com.juniverse.graphics.api.Bgi.kbhit;
import static com.juniverse.graphics.api.Bgi.line;
import static com.juniverse.graphics.api.Bgi.mouseX;
import static com.juniverse.graphics.api.Bgi.mouseY;
import static com.juniverse.graphics.api.Bgi.outTextXY;
import static com.juniverse.graphics.api.Bgi.pieSlice;
import static com.juniverse.graphics.api.Bgi.putPixel;
import static com.juniverse.graphics.api.Bgi.rectangle;
import static com.juniverse.graphics.api.Bgi.rgb;
import static com.juniverse.graphics.api.Bgi.setActivePage;
import static com.juniverse.graphics.api.Bgi.setBkColor;
import static com.juniverse.graphics.api.Bgi.setColor;
import static com.juniverse.graphics.api.Bgi.setFillStyle;
import static com.juniverse.graphics.api.Bgi.setLineStyle;
import static com.juniverse.graphics.api.Bgi.setTextJustify;
import static com.juniverse.graphics.api.Bgi.setTextStyle;
import static com.juniverse.graphics.api.Bgi.setVisualPage;
import static com.juniverse.graphics.api.Bgi.setWindowTitle;
import static com.juniverse.graphics.api.Bgi.setWriteMode;
import static com.juniverse.graphics.api.Bgi.swapBuffers;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.sin;

/**
 * Demonstration program: goes through the main functions of the
 * library in the style of a classic BGI program.
 * <p>
 * The shapes were designed on a 640x480 grid and are scaled with
 * {@link #sx(int)} and {@link #sy(int)}, so that the demonstration looks
 * complete in any graphics mode (for example VGAMED or IBM8514HI) and
 * also when the window is maximized (the drawing area grows with it).
 */
public class DemoGraphics {

    private static final Logger LOG = LoggerFactory.getLogger(DemoGraphics.class);

    private static final int ESC = 27;
    private static final int DESIGN_WIDTH = 640;
    private static final int DESIGN_HEIGHT = 480;

    public static void main(String[] args) {
		initGraph(DETECT, VGA, "");
        int errorCode = graphResult();
        if (errorCode != GR_OK) {
            LOG.atError()
                    .setMessage("Graphics error: {}")
                    .addArgument(() -> graphErrorMsg(errorCode))
                    .log();
            return;
        }

        try {
            setWindowTitle("SDL Graphics - Graphics demo");
            showUntilKey(DemoGraphics::drawShowcase, "Press a key to continue...");
            showUntilKey(DemoGraphics::drawPatterns, "Press a key to see the plasma...");

            plasma();

            animate();
            paintWithMouse();
        } finally {
            closeGraph();
        }
    }

    private static void plasma()
    {
        int i, x, y, r, g, b;
        int cols[][] = new int[3][256];
        double c;


        for (i = 0; i < 256; i++) {
            cols[0][i] = (int) abs( (128. - 127. * sin ((double) i * PI / 32.0)));
            cols[1][i] = (int) abs( (128. - 127. * sin ((double) i * PI / 64.)));
            cols[2][i] = (int) abs( (128. - 127. * sin ((double) i * PI / 128.)));
        }

        for (y = 0; y <= getMaxY(); y++) {
            for (x = 0; x <= getMaxX(); x++) {
                c = (sin(x/35.)*128. + sin(y/28.)*32. + sin((x+y)/16.)*64.);
                if (c > 255)
                    c = c - 256;
                if (c < 0)
                    c = 256 + c;
                r = cols[0][(int)c];
                if (r > 255)
                    r = r - 256;
                if (r < 0)
                    r = (int) (256 + c);
                g = cols[1][(int)c];
                if (g > 255)
                    g = g - 256;
                if (g < 0)
                    g = (int) (256 + c);
                b = cols[2][(int)c];
                if (b > 255)
                    b = b - 256;
                if (b < 0)
                    b = (int) (256 + c);

                putPixel(x, y, rgb(r, g, b));
            }
        }
        getch ();
    }

    /** Basic shapes, line styles and text. */
    private static void drawShowcase() {
        setBkColor(BgiColor.BLUE);
        clearDevice();

        setTextStyle(FontName.SANS_SERIF, TextDirection.HORIZONTAL, 2);
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        setColor(BgiColor.YELLOW);
        outTextXY(getMaxX() / 2, sy(8), "SDL Graphics for Java 21");
        setTextStyle(FontName.DEFAULT, TextDirection.HORIZONTAL, 1);
        setColor(BgiColor.LIGHT_GRAY);
        outTextXY(getMaxX() / 2, sy(32), getDriverName() + " - " + getModeName(getGraphMode()));

        setColor(BgiColor.WHITE);
        rectangle(sx(16), sy(56), sx(200), sy(176));
        setColor(BgiColor.LIGHT_GREEN);
        circle(sx(320), sy(116), sy(56));
        setColor(BgiColor.LIGHT_RED);
        ellipse(sx(512), sy(116), 0, 360, sx(88), sy(40));
        setColor(BgiColor.LIGHT_CYAN);
        arc(sx(320), sy(116), 30, 150, sy(72));

        LineStyle[] styles = LineStyle.values();
        for (int i = 0; i < styles.length - 1; i++) {
            setLineStyle(styles[i], 0, i % 2 == 0 ? NORM_WIDTH : THICK_WIDTH);
            setColor(BgiColor.fromCode(9 + i));
            line(sx(32), sy(200 + i * 16), sx(288), sy(200 + i * 16));
        }
        setLineStyle(LineStyle.USER_BIT, 0xF0F0, THICK_WIDTH);
        setColor(BgiColor.WHITE);
        line(sx(32), sy(280), sx(288), sy(280));
        setLineStyle(LineStyle.SOLID, 0, NORM_WIDTH);

        int pieX = sx(416);
        int pieY = sy(264);
        int pieRadius = sy(64);
        setFillStyle(FillPattern.SOLID, BgiColor.RED);
        pieSlice(pieX, pieY, 0, 120, pieRadius);
        setFillStyle(FillPattern.HATCH, BgiColor.GREEN);
        pieSlice(pieX, pieY, 120, 240, pieRadius);
        setFillStyle(FillPattern.SOLID, rgb(255, 160, 0));
        pieSlice(pieX, pieY, 240, 360, pieRadius);

        setColor(BgiColor.WHITE);
        setFillStyle(FillPattern.CLOSE_DOT, BgiColor.YELLOW);
        bar3d(sx(528), sy(208), sx(584), sy(320), sx(16), true);

        setTextStyle(FontName.DEFAULT, TextDirection.VERTICAL, 1);
        setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM);
        setColor(BgiColor.LIGHT_MAGENTA);
        outTextXY(sx(616), sy(336), "Vertical");

        setColor(BgiColor.WHITE);
        setFillStyle(FillPattern.SOLID, BgiColor.MAGENTA);
        fillPoly(sx(48), sy(336), sx(160), sy(320), sx(240), sy(384), sx(128), sy(432), sx(40), sy(400));
        setFillStyle(FillPattern.LIGHT_SLASH, BgiColor.LIGHT_CYAN);
        rectangle(sx(320), sy(352), sx(480), sy(432));
        circle(sx(400), sy(392), sy(24));
        floodFill(sx(320) + 4, sy(352) + 4, BgiColor.WHITE);
    }



    /** Shows the 12 standard fill patterns. */
    private static void drawPatterns() {
        setBkColor(BgiColor.BLACK);
        clearDevice();
        setTextStyle(FontName.DEFAULT, TextDirection.HORIZONTAL, 1);
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        FillPattern[] patterns = FillPattern.values();
        for (int i = 0; i < patterns.length - 1; i++) {
            int column = i % 4;
            int row = i / 4;
            int left = sx(24 + column * 152);
            int top = sy(32 + row * 136);
            setColor(BgiColor.WHITE);
            setFillStyle(patterns[i], BgiColor.fromCode(1 + i % 15));
            bar3d(left, top, left + sx(120), top + sy(96), 0, false);
            outTextXY(left + sx(60), top + sy(102), patterns[i].name());
        }
    }

    /** Double-buffered animation: a bouncing ball until a key is pressed. */
    private static void animate() {
        setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        int radius = sy(20);
        int x = radius + 10;
        int y = radius + 30;
        int dx = Math.max(2, sx(4));
        int dy = Math.max(2, sy(3));
        setActivePage(1);
        while (!kbhit()) {
            setBkColor(BgiColor.BLACK);
            clearDevice();
            setColor(BgiColor.LIGHT_GRAY);
            outTextXY(10, 10, "Double buffer: press a key to finish");
            setColor(BgiColor.WHITE);
            setFillStyle(FillPattern.SOLID, BgiColor.LIGHT_RED);
            fillEllipse(x, y, radius, radius);
            swapBuffers();
            x += dx;
            y += dy;
            if (x - radius <= 0 || x + radius >= getMaxX()) {
                dx = -dx;
            }
            if (y - radius <= 0 || y + radius >= getMaxY()) {
                dy = -dy;
            }
            delay(16);
        }
        getch();
        setActivePage(0);
        setVisualPage(0);
    }

    /** Free drawing with the mouse; ESC ends. */
    private static void paintWithMouse() {
        setBkColor(BgiColor.BLACK);
        clearDevice();
        setColor(BgiColor.YELLOW);
        outTextXY(10, 10, "Left: draw | Right: XOR rectangle | ESC: exit");
        outTextXY(10, 24, "You can maximize or resize the window");
        Point previous = null;
        while (true) {
            if (kbhit() && getch() == ESC) {
                return;
            }
            if (isMouseClick(MouseEventKind.LEFT_BUTTON_DOWN)) {
                previous = getMouseClick(MouseEventKind.LEFT_BUTTON_DOWN);
            }
            if (isMouseClick(MouseEventKind.LEFT_BUTTON_UP)) {
                getMouseClick(MouseEventKind.LEFT_BUTTON_UP);
                previous = null;
            }
            if (previous != null) {
                Point current = new Point(mouseX(), mouseY());
                setColor(BgiColor.LIGHT_GREEN);
                line(previous.x(), previous.y(), current.x(), current.y());
                previous = current;
            }
            if (isMouseClick(MouseEventKind.RIGHT_BUTTON_DOWN)) {
                Point click = getMouseClick(MouseEventKind.RIGHT_BUTTON_DOWN);
                setWriteMode(RasterOperation.XOR);
                setColor(BgiColor.WHITE);
                rectangle(click.x() - 40, click.y() - 30, click.x() + 40, click.y() + 30);
                setWriteMode(RasterOperation.COPY);
            }
            delay(10);
        }
    }

    /**
     * Draws a screen and waits for a key. If the user maximizes or resizes
     * the window, the drawing area grows and the screen is
     * redrawn with the new resolution.
     */
    private static void showUntilKey(Runnable screen, String message) {
        clearResizeEvent();
        drawScreen(screen, message);
        while (!kbhit()) {
            if (isResizeEvent()) {
                clearResizeEvent();
                drawScreen(screen, message);
            }
            delay(20);
        }
        getch();
    }

    private static void drawScreen(Runnable screen, String message) {
        screen.run();
        setTextStyle(FontName.DEFAULT, TextDirection.HORIZONTAL, 1);
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
        setColor(BgiColor.WHITE);
        outTextXY(getMaxX() / 2, getMaxY() - 5, message);
    }

    /** Scales a horizontal coordinate of the 640 design to the current resolution. */
    private static int sx(int x) {
        return x * (getMaxX() + 1) / DESIGN_WIDTH;
    }

    /** Scales a vertical coordinate of the 480 design to the current resolution. */
    private static int sy(int y) {
        return y * (getMaxY() + 1) / DESIGN_HEIGHT;
    }
}
