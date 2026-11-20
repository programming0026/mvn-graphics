package com.juniverse.graphics.examples.bgidemo;

import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.dto.FillSettings;
import com.juniverse.graphics.dto.LineSettings;
import com.juniverse.graphics.text.HorizontalAlignment;
import com.juniverse.graphics.dto.TextSettings;
import com.juniverse.graphics.text.VerticalAlignment;

import static com.juniverse.graphics.api.Bgi.*;

/**
 * Report of the configuration after starting the graphics mode
 * (equivalent to {@code ReportStatus} of {@code BGIDEMO.C}).
 */
public final class StatusReport {

    private final DemoFrame frame;

    public StatusReport(DemoFrame frame) {
        this.frame = frame;
    }

    public void run() {
        Viewport view = getViewSettings();
        LineSettings line = getLineSettings();
        FillSettings fill = getFillSettings();
        TextSettings text = getTextSettings();

        int x = 10;
        int y = 4;
        frame.mainWindow("Status report after InitGraph");
        setTextJustify(HorizontalAlignment.LEFT, VerticalAlignment.TOP);

        String driver = getDriverName();
        String mode = getModeName(frame.graphMode());

        y = frame.print(x, y, "Graphics device    : %-20s (%d)", driver, frame.graphDriver());
        y = frame.print(x, y, "Graphics mode      : %-20s (%d)", mode, frame.graphMode());
        y = frame.print(x, y, "Screen resolution  : ( 0, 0, %d, %d )", getMaxX(), getMaxY());
        y = frame.print(x, y, "Current view port  : ( %d, %d, %d, %d )",
                view.left(), view.top(), view.right(), view.bottom());
        y = frame.print(x, y, "Clipping           : %s", view.clip() ? "ON" : "OFF");
        y = frame.print(x, y, "Current position   : ( %d, %d )", getX(), getY());
        y = frame.print(x, y, "Colors available   : %d", frame.maxColors());
        y = frame.print(x, y, "Current color      : %d", getColor());
        y = frame.print(x, y, "Line style         : %s", DemoFrame.LINE_STYLE_NAMES.get(line.style().ordinal()));
        y = frame.print(x, y, "Line thickness     : %d", line.thickness());
        y = frame.print(x, y, "Current fill style : %s", DemoFrame.FILL_STYLE_NAMES.get(fill.pattern().ordinal()));
        y = frame.print(x, y, "Current fill color : %d", fill.color());
        y = frame.print(x, y, "Current font       : %s", DemoFrame.FONT_NAMES.get(text.font().ordinal()));
        y = frame.print(x, y, "Text direction     : %s", DemoFrame.TEXT_DIRECTION_NAMES.get(text.direction().ordinal()));
        y = frame.print(x, y, "Character size     : %d", text.charSize());
        y = frame.print(x, y, "Horizontal justify : %s",
                DemoFrame.HORIZONTAL_JUSTIFY_NAMES.get(text.horizontal().ordinal()));
        frame.print(x, y, "Vertical justify   : %s", DemoFrame.VERTICAL_JUSTIFY_NAMES.get(text.vertical().ordinal()));

        frame.pause();
    }
}
