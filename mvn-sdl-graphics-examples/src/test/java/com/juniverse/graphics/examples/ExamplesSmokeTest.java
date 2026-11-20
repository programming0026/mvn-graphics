package com.juniverse.graphics.examples;

import com.juniverse.graphics.api.Bgi;
import com.juniverse.graphics.examples.bgidemo.BgiDemo;
import com.juniverse.graphics.examples.dto.Example;
import com.juniverse.graphics.examples.mathdemo.FunctionGrapher;
import com.juniverse.graphics.image.ImageIoFileStore;
import com.juniverse.graphics.dto.InputEvent;
import com.juniverse.graphics.testing.FakePlatform;
import com.juniverse.graphics.text.FontRoutingRasterizer;
import com.juniverse.graphics.window.WindowManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the complete examples on a fake platform (without windows),
 * simulating the user pressing keys, to check that no
 * screen fails.
 */
public class ExamplesSmokeTest {

    /** Identifier that FakePlatform assigns to the first window. */
    private static final int FIRST_DISPLAY_ID = 1;
    private static final int ESC = 27;

    private FakePlatform platform;

    @BeforeEach
    public void setUp() {
        platform = new FakePlatform();
        WindowManager manager = new WindowManager(() -> platform, FontRoutingRasterizer.standard(),
                new ImageIoFileStore(), () -> { }, System::nanoTime);
        Bgi.configure(manager);
    }

    @Test
    public void bgiDemoRunsEveryScreen() {
        pressKeys(400);

        assertDoesNotThrow(() -> BgiDemo.main(new String[0]));
        assertFalse(Bgi.windowManager().hasOpenWindows());
        // CRTModeDemo uses setGraphMode with the same resolution: the window is reused.
        assertEquals(1, platform.displays().size());
    }

    @Test
    public void functionGrapherShowsTableAndPlot() {
        pressKeys(2);

        assertDoesNotThrow(() -> FunctionGrapher.main(new String[0]));
        assertFalse(Bgi.windowManager().hasOpenWindows());
        assertTrue(platform.displays().get(0).isClosed());
    }

    @Test
    public void bgiDemoEscapeEndsCleanlyWithoutExiting() {
        // ESC on the first pause: it used to be exit(); now the demo ends and closes its window.
        platform.enqueue(new InputEvent.KeyTyped(FIRST_DISPLAY_ID, ESC));

        assertDoesNotThrow(() -> BgiDemo.main(new String[0]));
        assertFalse(Bgi.windowManager().hasOpenWindows());
    }

    @Test
    public void menuListsAllMainExamplesInOrder() {
        assertEquals(List.of("DemoGraphics", "FireEffect", "FourierSeries", "FunctionGrapher",
                        "SevenSegmentDisplay", "BgiDemo"),
                Main.EXAMPLES.stream().map(Example::title).toList());
    }

    @Test
    public void menuReturnsChosenExampleAndClosesItsWindow() {
        platform.enqueue(new InputEvent.KeyTyped(FIRST_DISPLAY_ID, 'x')); // invalid key: ignored
        platform.enqueue(new InputEvent.KeyTyped(FIRST_DISPLAY_ID, '2'));

        Optional<Example> choice = Main.chooseExample();

        assertEquals("FireEffect", choice.orElseThrow().title());
        assertFalse(Bgi.windowManager().hasOpenWindows());
    }

    @Test
    public void menuEscapeExits() {
        platform.enqueue(new InputEvent.KeyTyped(FIRST_DISPLAY_ID, ESC));

        assertTrue(Main.chooseExample().isEmpty());
        assertFalse(Bgi.windowManager().hasOpenWindows());
    }

    private void pressKeys(int count) {
        for (int i = 0; i < count; i++) {
            platform.enqueue(new InputEvent.KeyTyped(FIRST_DISPLAY_ID, ' '));
        }
    }
}
