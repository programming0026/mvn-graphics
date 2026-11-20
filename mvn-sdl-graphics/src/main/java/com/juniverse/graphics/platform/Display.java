package com.juniverse.graphics.platform;

import com.juniverse.graphics.dto.Size;
import com.juniverse.graphics.raster.FrameBuffer;

/**
 * Native window able to show the content of a page.
 */
public interface Display extends AutoCloseable {

    /** Native identifier the platform uses to label events. */
    int id();

    /**
     * Shows the top-left region of the page on screen. If the
     * region size changed since the last time, the window adapts.
     *
     * @param page       page to show
     * @param area       visible region (may be smaller than the page)
     * @param borderArgb ARGB color of the window area not covered by the page
     */
    void present(FrameBuffer page, Size area, int borderArgb);

    /** Actual size in pixels of the window's client area. */
    Size outputSize();

    /** Changes how the page fits the window. */
    void setScalingMode(ScalingMode mode);

    /** Changes the window title. */
    void setTitle(String title);

    /** Releases the native resources of the window. */
    @Override
    void close();
}
