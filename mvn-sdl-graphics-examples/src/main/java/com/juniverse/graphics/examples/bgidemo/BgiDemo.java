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
 * Port de {@code BGIDEMO.C}, la demostración oficial de la Borland Graphics
 * Interface: recorre colores, paleta, píxeles, imágenes, barras, arcos,
 * círculos, pasteles, líneas, fuentes, rellenos y polígonos.
 * <p>
 * Cada pantalla espera una tecla; ESC termina en cualquier momento. Las
 * demostraciones están agrupadas por tema en clases de este paquete y
 * comparten el marco común de {@link DemoFrame}.
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
            // ESC o error al iniciar: se termina sin cerrar la aplicación (antes era exit()).
            LOG.atInfo()
                    .setMessage("BgiDemo ended early: {}")
                    .addArgument(e::getMessage)
                    .log();
        } finally {
            closeGraph();
        }
    }

    /** Ejecuta todas las demostraciones en el orden del original. */
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

    /** Pantalla de despedida (SayGoodbye). */
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
