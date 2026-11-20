package com.juniverse.graphics.examples.bgidemo;

import com.juniverse.graphics.color.BgiColor;
import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.text.FontName;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.text.TextDirection;
import com.juniverse.graphics.text.VerticalAlignment;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Mode change demonstration: {@code CRTModeDemo}.
 * <p>
 * In DOS, {@code restorecrtmode()} went back to text mode and the program
 * wrote with {@code printf}. Here there is no text mode: the text screen
 * is simulated inside the same window with the 8x8 font, in light
 * gray on black, like the DOS console.
 */
public final class ModeDemo {

    private static final int TEXT_LINE_HEIGHT = 16;

    private final DemoFrame frame;

    public ModeDemo(DemoFrame frame) {
        this.frame = frame;
    }

    public void crtModeDemo() {
        frame.mainWindow("SetGraphMode / RestoreCRTMode demo");
        Viewport vp = getViewSettings();
        int mode = getGraphMode();
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        outTextXY((vp.right() - vp.left()) / 2, (vp.bottom() - vp.top()) / 2, "Now you are in graphics mode...");
        frame.statusLine("Press any key for text mode...");
        getch();

        restoreCrtMode();
        showTextModeScreen("Now you are in text mode.", "", "Press any key to go back to graphics...");
        getch();

        setGraphMode(mode);
        frame.mainWindow("SetGraphMode / RestoreCRTMode demo");
        setTextJustify(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        outTextXY((vp.right() - vp.left()) / 2, (vp.bottom() - vp.top()) / 2, "Back in Graphics Mode...");
        frame.pause();
    }

    /** Simulates the text console: black screen and gray text from the top corner. */
    private void showTextModeScreen(String... lines) {
        setViewport(0, 0, frame.maxX(), frame.maxY(), true);
        setBkColor(BgiColor.BLACK);
        clearDevice();
        setColor(BgiColor.LIGHT_GRAY);
        frame.changeTextStyle(FontName.DEFAULT, TextDirection.HORIZONTAL, 1);
        setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        int y = 0;
        for (String line : lines) {
            outTextXY(0, y, line);
            y += TEXT_LINE_HEIGHT;
        }
    }
}
