package com.juniverse.graphics.examples.bgidemo;

import com.juniverse.graphics.color.BgiColor;
import com.juniverse.graphics.dto.Point;
import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.style.LineStyle;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.text.VerticalAlignment;

import java.util.ArrayList;
import java.util.List;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Line demonstrations: {@code LineRelDemo}, {@code LineToDemo},
 * {@code LineStyleDemo} and {@code UserLineStyleDemo}.
 */
public final class LineDemos {

    private static final int CHORD_POINTS = 15;

    private final DemoFrame frame;

    public LineDemos(DemoFrame frame) {
        this.frame = frame;
    }

    /** Draws a "tesseract" with moveRel and lineRel (LineRelDemo). */
    public void lineRelDemo() {
        frame.mainWindow("MoveRel / LineRel Demonstration");
        frame.statusLine("Press any key to continue, ESC to Abort");

        Viewport vp = getViewSettings();
        int cx = (vp.right() - vp.left()) / 2;
        int cy = (vp.bottom() - vp.top()) / 2;
        int h = (vp.bottom() - vp.top()) / 8;
        int w = (vp.right() - vp.left()) / 9;
        int dx = 2 * w;
        int dy = 2 * h;

        setColor(BgiColor.BLACK);
        setFillStyle(FillPattern.SOLID, BgiColor.BLUE);
        bar(0, 0, vp.right() - vp.left(), vp.bottom() - vp.top());

        setFillStyle(FillPattern.SOLID, BgiColor.WHITE);
        fillPoly(List.of(
                new Point(cx - dx, cy - dy),
                new Point(cx - (dx - w), cy - (dy + h)),
                new Point(cx + dx, cy - (dy + h)),
                new Point(cx + dx, cy + dy),
                new Point(cx + (dx - w), cy + (dy + h)),
                new Point(cx - dx, cy + (dy + h)),
                new Point(cx - dx, cy - dy)));

        setFillStyle(FillPattern.SOLID, BgiColor.BLUE);
        fillPoly(List.of(
                new Point(cx - (w / 2), cy + h),
                new Point(cx + (w / 2), cy + h),
                new Point(cx + (w / 2), cy - h),
                new Point(cx - (w / 2), cy - h),
                new Point(cx - (w / 2), cy + h)));

        moveTo(cx - dx, cy - dy);
        lineRel(w, -h);
        lineRel(3 * w, 0);
        lineRel(0, 5 * h);
        lineRel(-w, h);
        lineRel(-3 * w, 0);
        lineRel(0, -5 * h);

        moveRel(w, -h);
        lineRel(0, 5 * h);
        lineRel(w + (w / 2), 0);
        lineRel(0, -3 * h);
        lineRel(w / 2, -h);
        lineRel(0, 5 * h);

        moveRel(0, -5 * h);
        lineRel(-(w + (w / 2)), 0);
        lineRel(0, 3 * h);
        lineRel(-w / 2, h);

        moveRel(w / 2, -h);
        lineRel(w, 0);

        moveRel(0, -2 * h);
        lineRel(-w, 0);

        frame.pause();
    }

    /** Joins 15 points of a circle with chords (LineToDemo). */
    public void lineToDemo() {
        frame.mainWindow("MoveTo / LineTo Demonstration");

        Viewport vp = getViewSettings();
        int h = vp.bottom() - vp.top();
        int w = vp.right() - vp.left();
        int centerX = w / 2;
        int centerY = h / 2;
        int radius = (int) ((h - 30) / (frame.aspectRatio() * 2));
        int step = 360 / CHORD_POINTS;

        List<Point> points = new ArrayList<>();
        for (int i = 0, angle = 0; i < CHORD_POINTS; i++, angle += step) {
            double radians = Math.toRadians(angle);
            points.add(new Point(
                    centerX + (int) (Math.cos(radians) * radius),
                    centerY - (int) (Math.sin(radians) * radius * frame.aspectRatio())));
        }

        circle(centerX, centerY, radius);
        for (int i = 0; i < CHORD_POINTS; i++) {
            for (int j = i; j < CHORD_POINTS; j++) {
                moveTo(points.get(i).x(), points.get(i).y());
                lineTo(points.get(j).x(), points.get(j).y());
            }
        }
        frame.pause();
    }

    /** Shows the 4 line styles in normal and thick widths (LineStyleDemo). */
    public void lineStyleDemo() {
        frame.mainWindow("Pre-defined line styles");

        Viewport vp = getViewSettings();
        int w = vp.right() - vp.left();
        int x = 35;
        int y = 10;
        int step = w / 11;

        setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        outTextXY(x, y, "Normal Width");
        x = drawStyleColumns(x, y, step, vp, NORM_WIDTH);

        x += 2 * step;
        setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        outTextXY(x, y, "Thick Width");
        drawStyleColumns(x, y, step, vp, THICK_WIDTH);

        setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        frame.pause();
    }

    private static int drawStyleColumns(int x, int y, int step, Viewport vp, int thickness) {
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        for (int style = 0; style < 4; style++) {
            setLineStyle(LineStyle.fromCode(style), 0, thickness);
            line(x, y + 20, x, vp.bottom() - 40);
            outTextXY(x, vp.bottom() - 30, String.valueOf(style));
            x += step;
        }
        return x;
    }

    /**
     * Lines with user patterns: first the 16 bits of the pattern are
     * turned on one by one and then turned off (UserLineStyleDemo).
     * <p>
     * The original turned the bits off with {@code style & !(0x8000 >> i)}; the
     * {@code !} (logical negation) is 0, so it cleared the whole pattern at
     * once. Here {@code ~} (bitwise negation) is used, which was the intention.
     */
    public void userLineStyleDemo() {
        frame.mainWindow("User defined line styles");

        Viewport vp = getViewSettings();
        int h = vp.bottom() - vp.top();
        int x = 4;
        int y = 10;
        int style = 0;
        int bit = 0;
        boolean setting = true;

        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        while (x < vp.right() - 2) {
            if (setting) {
                style |= 1 << bit;
            } else {
                style &= ~(0x8000 >> bit) & 0xFFFF;
            }
            setLineStyle(LineStyle.USER_BIT, style, NORM_WIDTH);
            line(x, y, x, h - y);

            x += 5;
            bit = (bit + 1) % 16;
            if (style == 0xFFFF) {
                setting = false;
                bit = 0;
            } else if (style == 0) {
                setting = true;
            }
        }
        setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        frame.pause();
    }
}
