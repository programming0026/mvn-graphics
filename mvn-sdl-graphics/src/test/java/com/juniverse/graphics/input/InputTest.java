package com.juniverse.graphics.input;

import com.juniverse.graphics.dto.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputTest {

    @Test
    public void extendedKeysArePrefixedWithZero() {
        KeyboardBuffer keyboard = new KeyboardBuffer();
        keyboard.push('a');
        keyboard.pushExtended(ExtendedKey.UP);

        assertEquals('a', keyboard.poll());
        assertEquals(0, keyboard.poll());
        assertEquals(ExtendedKey.UP.code(), keyboard.poll());
        assertFalse(keyboard.hasKey());
    }

    @Test
    public void mouseKeepsOnlyLastClickUnlessQueuing() {
        MouseTracker mouse = new MouseTracker();
        mouse.record(MouseEventKind.LEFT_BUTTON_DOWN, 1, 1);
        mouse.record(MouseEventKind.LEFT_BUTTON_DOWN, 2, 2);
        assertEquals(new Point(2, 2), mouse.pollClick(MouseEventKind.LEFT_BUTTON_DOWN));
        assertEquals(Point.NONE, mouse.pollClick(MouseEventKind.LEFT_BUTTON_DOWN));

        mouse.setQueueStatus(MouseEventKind.LEFT_BUTTON_DOWN, true);
        mouse.record(MouseEventKind.LEFT_BUTTON_DOWN, 3, 3);
        mouse.record(MouseEventKind.LEFT_BUTTON_DOWN, 4, 4);
        assertEquals(new Point(3, 3), mouse.pollClick(MouseEventKind.LEFT_BUTTON_DOWN));
        assertEquals(new Point(4, 4), mouse.pollClick(MouseEventKind.LEFT_BUTTON_DOWN));
    }

    @Test
    public void mouseHandlerIsInvoked() {
        MouseTracker mouse = new MouseTracker();
        int[] received = new int[2];
        mouse.registerHandler(MouseEventKind.RIGHT_BUTTON_UP, (x, y) -> {
            received[0] = x;
            received[1] = y;
        });
        mouse.record(MouseEventKind.RIGHT_BUTTON_UP, 7, 9);

        assertEquals(7, received[0]);
        assertEquals(9, received[1]);
        assertTrue(mouse.hasClick(MouseEventKind.RIGHT_BUTTON_UP));
        assertEquals(7, mouse.x());
    }
}
