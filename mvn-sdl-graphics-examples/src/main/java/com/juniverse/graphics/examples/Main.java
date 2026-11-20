package com.juniverse.graphics.examples;

import com.juniverse.graphics.color.BgiColor;
import com.juniverse.graphics.examples.bgidemo.BgiDemo;
import com.juniverse.graphics.examples.bgidemo.DemoGraphics;
import com.juniverse.graphics.examples.dto.Example;
import com.juniverse.graphics.examples.firedemo.FireEffect;
import com.juniverse.graphics.examples.mathdemo.FourierSeries;
import com.juniverse.graphics.examples.mathdemo.FunctionGrapher;
import com.juniverse.graphics.examples.mathdemo.SevenSegmentDisplay;
import com.juniverse.graphics.platform.ScalingMode;
import com.juniverse.graphics.text.FontName;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.text.TextDirection;
import com.juniverse.graphics.text.VerticalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Main menu of the examples.
 * <p>
 * Shows a window with the numbered list of examples. When the
 * number of one is pressed, the menu closes, the example runs in its own
 * window and, when it finishes, the menu appears again. ESC exits the program.
 * <p>
 * To add an example it is enough to add it to {@link #EXAMPLES}.
 */
public final class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final int ESC = 27;
    private static final String[] NO_ARGUMENTS = {};

    /** Available examples, in the order they appear in the menu. */
    public static final List<Example> EXAMPLES = List.of(
            new Example("DemoGraphics", "Shapes, patterns, plasma, animation and mouse",
                    () -> DemoGraphics.main(NO_ARGUMENTS)),
            new Example("FireEffect", "Fire effect in VGA mode",
                    () -> FireEffect.main(NO_ARGUMENTS)),
            new Example("FourierSeries", "Square wave from a Fourier series",
                    () -> FourierSeries.main(NO_ARGUMENTS)),
            new Example("FunctionGrapher", "Tabulates and plots a function",
                    () -> FunctionGrapher.main(NO_ARGUMENTS)),
            new Example("SevenSegmentDisplay", "Seven-segment display",
                    () -> SevenSegmentDisplay.main(NO_ARGUMENTS)),
            new Example("BgiDemo", "Official Borland BGI demonstration",
                    () -> BgiDemo.main(NO_ARGUMENTS)));

    private static final int FIRST_ITEM_Y = 112;
    private static final int ITEM_HEIGHT = 50;
    private static final int KEY_X = 60;
    private static final int TEXT_X = 130;

    private Main() {
    }

    public static void main(String[] args) {
        LOG.info("SDL Graphics examples menu started");
        Optional<Example> choice;
        while ((choice = chooseExample()).isPresent()) {
            run(choice.get());
        }
        LOG.info("SDL Graphics examples menu closed");
    }

    /**
     * Opens the menu and waits for the user to choose an example.
     *
     * @return the chosen example, or empty if ESC was pressed
     */
    public static Optional<Example> chooseExample() {
        initGraph(DETECT, VGA, "");
        int errorCode = graphResult();
        if (errorCode != GR_OK) {
            LOG.atError()
                    .setMessage("Could not open the menu: {}")
                    .addArgument(() -> graphErrorMsg(errorCode))
                    .log();
            return Optional.empty();
        }
        try {
            setWindowTitle("SDL Graphics - Examples");
            setWindowScaling(ScalingMode.KEEP_ASPECT);
            drawMenu();
            return readChoice();
        } finally {
            closeGraph();
        }
    }

    /** Reads keys until a valid example number or ESC is obtained. */
    private static Optional<Example> readChoice() {
        while (true) {
            int key = getch();
            if (key == ESC) {
                return Optional.empty();
            }
            if (key == 0) {
                getch();
                continue;
            }
            int index = key - '1';
            if (index >= 0 && index < EXAMPLES.size()) {
                return Optional.of(EXAMPLES.get(index));
            }
            LOG.debug("Ignored key in menu: {}", key);
        }
    }

    /**
     * Runs an example. If it fails, the error is logged and execution returns to the menu
     * instead of terminating the application.
     */
    private static void run(Example example) {
        String title = example.title();
        LOG.info("Running example {}", title);
        try {
            example.entryPoint().run();
            LOG.info("Example {} finished", title);
        } catch (RuntimeException e) {
            LOG.error("Example {} failed", title, e);
            closeGraph();
        }
    }

    /** Draws the menu: title, numbered list of examples and instructions. */
    private static void drawMenu() {
        setBkColor(BgiColor.BLUE);
        clearDevice();
        setColor(BgiColor.WHITE);
        rectangle(4, 4, getMaxX() - 4, getMaxY() - 4);
        rectangle(8, 8, getMaxX() - 8, getMaxY() - 8);

        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        setTextStyle(FontName.DEFAULT, TextDirection.HORIZONTAL, 3);
        setColor(BgiColor.YELLOW);
        outTextXY(getMaxX() / 2, 30, "SDL Graphics");
        setTextStyle(FontName.DEFAULT, TextDirection.HORIZONTAL, 1);
        setColor(BgiColor.LIGHT_CYAN);
        outTextXY(getMaxX() / 2, 64, "BGI (graphics.h) examples for Java 21 with SDL 3");

        setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        for (int i = 0; i < EXAMPLES.size(); i++) {
            Example example = EXAMPLES.get(i);
            int y = FIRST_ITEM_Y + i * ITEM_HEIGHT;
            setTextStyle(FontName.DEFAULT, TextDirection.HORIZONTAL, 2);
            setColor(BgiColor.YELLOW);
            outTextXY(KEY_X, y, "[" + (i + 1) + "]");
            setColor(BgiColor.WHITE);
            outTextXY(TEXT_X, y, example.title());
            setTextStyle(FontName.DEFAULT, TextDirection.HORIZONTAL, 1);
            setColor(BgiColor.LIGHT_GRAY);
            outTextXY(TEXT_X, y + 20, example.description());
        }

        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
        setColor(BgiColor.YELLOW);
        outTextXY(getMaxX() / 2, getMaxY() - 20,
                "Press 1-" + EXAMPLES.size() + " to run an example    ESC: exit");
    }
}
