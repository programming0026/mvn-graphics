package com.juniverse.graphics.drawing;

import com.juniverse.graphics.color.BgiColor;
import com.juniverse.graphics.color.Palette;
import com.juniverse.graphics.dto.ArcCoordinates;
import com.juniverse.graphics.dto.AspectRatio;
import com.juniverse.graphics.dto.Point;
import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.style.FillPattern;
import com.juniverse.graphics.dto.FillSettings;
import com.juniverse.graphics.dto.LineSettings;
import com.juniverse.graphics.style.PatternMask;
import com.juniverse.graphics.style.RasterOperation;

import java.util.Objects;

/**
 * Graphics state of a window: colors, styles, viewport and current position.
 * <p>
 * Equivalent to the drawing part of WinBGI's {@code WindowData}
 * structure. It only stores data; it draws nothing.
 */
public final class DrawingState {

    private int screenWidth;
    private int screenHeight;
    private final Palette palette = new Palette();

    private int color;
    private int backgroundColor;
    private LineSettings lineSettings;
    private FillSettings fillSettings;
    private PatternMask userFillPattern;
    private RasterOperation writeMode;
    private Viewport viewport;
    private Point position;
    private ArcCoordinates lastArc;
    private AspectRatio aspectRatio;

    public DrawingState(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        reset();
    }

    /** Restores all default values (equivalent to {@code graphdefaults}). */
    public void reset() {
        palette.reset();
        color = BgiColor.WHITE.code();
        backgroundColor = BgiColor.BLACK.code();
        lineSettings = LineSettings.DEFAULT;
        fillSettings = FillSettings.DEFAULT;
        userFillPattern = FillPattern.SOLID.mask();
        writeMode = RasterOperation.COPY;
        viewport = Viewport.fullScreen(screenWidth, screenHeight);
        position = new Point(0, 0);
        lastArc = ArcCoordinates.EMPTY;
        aspectRatio = AspectRatio.SQUARE;
    }

    /**
     * Updates the screen size (for example, when maximizing the
     * window). If the viewport covered the whole screen, it grows with it.
     */
    public void resizeScreen(int width, int height) {
        boolean fullScreenViewport = viewport.equals(Viewport.fullScreen(screenWidth, screenHeight));
        screenWidth = width;
        screenHeight = height;
        if (fullScreenViewport) {
            viewport = Viewport.fullScreen(width, height);
        }
    }

    public int screenWidth() {
        return screenWidth;
    }

    public int screenHeight() {
        return screenHeight;
    }

    public Palette palette() {
        return palette;
    }

    public int color() {
        return color;
    }

    public void setColor(int colorCode) {
        this.color = colorCode;
    }

    /** Current drawing color converted to ARGB. */
    public int colorArgb() {
        return palette.toArgb(color);
    }

    public int backgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int colorCode) {
        this.backgroundColor = colorCode;
    }

    /** Current background color converted to ARGB. */
    public int backgroundArgb() {
        return palette.toArgb(backgroundColor);
    }

    public LineSettings lineSettings() {
        return lineSettings;
    }

    public void setLineSettings(LineSettings lineSettings) {
        this.lineSettings = Objects.requireNonNull(lineSettings);
    }

    public FillSettings fillSettings() {
        return fillSettings;
    }

    public void setFillSettings(FillSettings fillSettings) {
        this.fillSettings = Objects.requireNonNull(fillSettings);
    }

    public PatternMask userFillPattern() {
        return userFillPattern;
    }

    public void setUserFillPattern(PatternMask userFillPattern) {
        this.userFillPattern = Objects.requireNonNull(userFillPattern);
    }

    /** Effective fill mask according to the current pattern. */
    public PatternMask activeFillMask() {
        return fillSettings.pattern() == FillPattern.USER ? userFillPattern : fillSettings.pattern().mask();
    }

    public RasterOperation writeMode() {
        return writeMode;
    }

    public void setWriteMode(RasterOperation writeMode) {
        this.writeMode = Objects.requireNonNull(writeMode);
    }

    public Viewport viewport() {
        return viewport;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = Objects.requireNonNull(viewport);
    }

    /** Current position (CP), relative to the viewport. */
    public Point position() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = Objects.requireNonNull(position);
    }

    public ArcCoordinates lastArc() {
        return lastArc;
    }

    public void setLastArc(ArcCoordinates lastArc) {
        this.lastArc = Objects.requireNonNull(lastArc);
    }

    public AspectRatio aspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(AspectRatio aspectRatio) {
        this.aspectRatio = Objects.requireNonNull(aspectRatio);
    }
}
