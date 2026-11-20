package com.juniverse.graphics.examples.bgidemo;

import com.juniverse.graphics.examples.exception.ExampleAbortedException;
import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.platform.ScalingMode;
import com.juniverse.graphics.text.FontName;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.text.TextDirection;
import com.juniverse.graphics.text.VerticalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Port of {@code BGIDEMO.C}, the official demonstration of the Borland Graphics
 * Interface: it goes through colors, palette, pixels, images, bars, arcs,
 * circles, pies, lines, fonts, fills and polygons.
 * <p>
 * Each screen waits for a key; ESC ends at any moment. The
 * demonstrations are grouped by topic in classes of this package and
 * share the common frame of {@link DemoFrame}.
 * <p>
 */
public final class BgiDemo {

    private static final Logger LOG = LoggerFactory.getLogger(BgiDemo.class);

    private BgiDemo() {
    }

    public static void main(String[] args) {
        try {
            DemoFrame frame = DemoFrame.initialize();
            setWindowTitle("SDL Graphics - Borland BGI demo (BGIDEMO.C)");
            setWindowScaling(ScalingMode.KEEP_ASPECT);
            runAll(frame);
        } catch (ExampleAbortedException e) {
            // ESC or init error: ends without closing the application (it used to be exit()).
            LOG.atInfo()
                    .setMessage("BgiDemo ended early: {}")
                    .addArgument(e::getMessage)
                    .log();
        } finally {
            closeGraph();
        }
    }

    /** Runs all the demonstrations in the original order. */
    private static void runAll(DemoFrame frame) {
        ColorDemos colors = new ColorDemos(frame);
        PixelDemos pixels = new PixelDemos(frame);
        BarDemos bars = new BarDemos(frame);
        CurveDemos curves = new CurveDemos(frame);
        LineDemos lines = new LineDemos(frame);
        TextDemos texts = new TextDemos(frame);
        FillDemos fills = new FillDemos(frame);

        new StatusReport(frame).run();
        colors.colorDemo();
        if (frame.graphDriver() == EGA || frame.graphDriver() == VGA) {
            colors.paletteDemo();
        }
        pixels.putPixelDemo();
        pixels.putImageDemo();
        bars.bar3dDemo();
        bars.barDemo();
        bars.randomBars();
        curves.arcDemo();
        curves.circleDemo();
        curves.pieDemo();
        lines.lineRelDemo();
        lines.lineToDemo();
        lines.lineStyleDemo();
        lines.userLineStyleDemo();
        texts.textDump();
        texts.textDemo();
        new ModeDemo(frame).crtModeDemo();
        fills.fillStyleDemo();
        fills.fillPatternDemo();
        fills.polyDemo();
        sayGoodbye(frame);
    }

    /** Goodbye screen (SayGoodbye). */
    private static void sayGoodbye(DemoFrame frame) {
        frame.mainWindow("== Finale ==");

        Viewport view = getViewSettings();
        frame.changeTextStyle(FontName.TRIPLEX, TextDirection.HORIZONTAL, 4);
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        int h = view.bottom() - view.top();
        int w = view.right() - view.left();
        outTextXY(w / 2, h / 2, "That's all, folks!");

        frame.statusLine("Press any key to EXIT");
        getch();
        clearDevice();
    }
}
