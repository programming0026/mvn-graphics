package com.juniverse.graphics.examples.bgidemo;

import com.juniverse.graphics.color.BgiColor;
import com.juniverse.graphics.dto.ArcCoordinates;
import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.text.FontName;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.text.TextDirection;
import com.juniverse.graphics.text.VerticalAlignment;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Curve demonstrations: {@code ArcDemo}, {@code CircleDemo} and
 * {@code PieDemo}.
 */
public final class CurveDemos {

    private final DemoFrame frame;

    public CurveDemos(DemoFrame frame) {
        this.frame = frame;
    }

    /** Random arcs joined to their center until a key is pressed (ArcDemo). */
    public void arcDemo() {
        frame.mainWindow("Arc Demonstration");
        frame.statusLine("ESC Aborts - Press a Key to stop");

        int maxRadius = frame.maxY() / 10;
        while (!kbhit()) {
            setColor(frame.random(frame.maxColors() - 1) + 1);
            int endAngle = frame.random(358) + 1;
            arc(frame.random(frame.maxX()), frame.random(frame.maxY()), frame.random(endAngle), endAngle, maxRadius);
            ArcCoordinates arc = getArcCoords();
            line(arc.x(), arc.y(), arc.xStart(), arc.yStart());
            line(arc.x(), arc.y(), arc.xEnd(), arc.yEnd());
        }
        frame.pause();
    }

    /** Random circles until a key is pressed (CircleDemo). */
    public void circleDemo() {
        frame.mainWindow("Circle Demonstration");
        frame.statusLine("ESC Aborts - Press a Key to stop");

        int maxRadius = frame.maxY() / 10;
        while (!kbhit()) {
            setColor(frame.random(frame.maxColors() - 1) + 1);
            circle(frame.random(frame.maxX()), frame.random(frame.maxY()), frame.random(maxRadius));
        }
        frame.pause();
    }

    /** Pie chart with four slices and their percentages (PieDemo). */
    public void pieDemo() {
        frame.mainWindow("Pie Chart Demonstration");

        Viewport vp = getViewSettings();
        int centerX = (vp.right() - vp.left()) / 2;
        int centerY = (vp.bottom() - vp.top()) / 2 + 20;
        int radius = (vp.bottom() - vp.top()) / 3;
        double pieSize = (vp.bottom() - vp.top()) / 4.0;
        while (frame.aspectRatio() * radius < pieSize) {
            radius++;
        }
        int labelRadius = radius + radius / 5;

        frame.changeTextStyle(FontName.TRIPLEX, TextDirection.HORIZONTAL, 4);
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        outTextXY(frame.maxX() / 2, 6, "This is a Pie Chart");
        frame.changeTextStyle(FontName.TRIPLEX, TextDirection.HORIZONTAL, 1);

        setFillStyle(FillPattern.SOLID, BgiColor.RED);
        pieSlice(centerX + 10, centerY - adjustAspect(10), 0, 90, radius);
        label(centerX, centerY, labelRadius, 45, HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM, "25 %");

        setFillStyle(FillPattern.WIDE_DOT, BgiColor.GREEN);
        pieSlice(centerX, centerY, 90, 135, radius);
        label(centerX, centerY, labelRadius, 113, HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM, "12.5 %");

        setFillStyle(FillPattern.INTERLEAVE, BgiColor.YELLOW);
        pieSlice(centerX - 10, centerY, 135, 225, radius);
        label(centerX, centerY, labelRadius, 180, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER, "25 %");

        setFillStyle(FillPattern.HATCH, BgiColor.BLUE);
        pieSlice(centerX, centerY, 225, 360, radius);
        label(centerX, centerY, labelRadius, 293, HorizontalAlignment.LEFT, VerticalAlignment.TOP, "37.5 %");

        frame.pause();
    }

    /** Writes a label at {@code labelRadius} from the center, at the given angle. */
    private void label(int centerX, int centerY, int labelRadius, double degrees,
                       HorizontalAlignment horizontal, VerticalAlignment vertical, String text) {
        double radians = Math.toRadians(degrees);
        int x = centerX + (int) (Math.cos(radians) * labelRadius);
        int y = centerY - (int) (Math.sin(radians) * labelRadius * frame.aspectRatio());
        setTextJustify(horizontal, vertical);
        outTextXY(x, y, text);
    }

    /** Corrects a vertical distance according to the aspect ratio (adjasp macro). */
    private int adjustAspect(int y) {
        return (int) (frame.aspectRatio() * y);
    }
}
