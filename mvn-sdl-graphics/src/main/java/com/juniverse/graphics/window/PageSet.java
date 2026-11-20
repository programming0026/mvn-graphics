package com.juniverse.graphics.window;

import com.juniverse.graphics.raster.FrameBuffer;

/**
 * Set of pages of a window with the active page (where drawing happens)
 * and the visual page (the one that is shown).
 */
public final class PageSet {

    /** Maximum number of pages (same as WinBGI). */
    public static final int MAX_PAGES = 4;

    private final FrameBuffer[] pages = new FrameBuffer[MAX_PAGES];
    private int activePage;
    private int visualPage;

    /**
     * Creates the pages filled with the initial background color.
     *
     * @param backgroundArgb opaque ARGB color the pages are initialized with
     */
    public PageSet(int width, int height, int backgroundArgb) {
        for (int i = 0; i < MAX_PAGES; i++) {
            pages[i] = new FrameBuffer(width, height);
            pages[i].fill(backgroundArgb);
        }
    }

    /**
     * Guarantees that the pages are at least the given size. Pages
     * only grow, they never shrink: this way, if the window shrinks or is
     * minimized, what was drawn outside the visible part is not lost. The
     * content is kept anchored at the top-left and the new area is
     * filled with the given color.
     */
    public void ensureCapacity(int width, int height, int backgroundArgb) {
        int newWidth = Math.max(width, capacityWidth());
        int newHeight = Math.max(height, capacityHeight());
        if (newWidth == capacityWidth() && newHeight == capacityHeight()) {
            return;
        }
        for (int i = 0; i < MAX_PAGES; i++) {
            FrameBuffer resized = new FrameBuffer(newWidth, newHeight);
            resized.fill(backgroundArgb);
            resized.copyOverlapFrom(pages[i]);
            pages[i] = resized;
        }
    }

    /** Fills all pages with a color. */
    public void clearAll(int argb) {
        for (FrameBuffer page : pages) {
            page.fill(argb);
        }
    }

    /** Actual in-memory width of the pages. */
    public int capacityWidth() {
        return pages[0].width();
    }

    /** Actual in-memory height of the pages. */
    public int capacityHeight() {
        return pages[0].height();
    }

    /** Page currently being drawn on. */
    public FrameBuffer active() {
        return pages[activePage];
    }

    /** Page shown on screen. */
    public FrameBuffer visual() {
        return pages[visualPage];
    }

    public int activeIndex() {
        return activePage;
    }

    public int visualIndex() {
        return visualPage;
    }

    /** Tells whether the page being shown is the same one being drawn on. */
    public boolean isViewingActivePage() {
        return activePage == visualPage;
    }

    /**
     * Changes the active page.
     *
     * @return false if the page number is not valid
     */
    public boolean setActive(int page) {
        if (!isValid(page)) {
            return false;
        }
        activePage = page;
        return true;
    }

    /**
     * Changes the visual page.
     *
     * @return false if the page number is not valid
     */
    public boolean setVisual(int page) {
        if (!isValid(page)) {
            return false;
        }
        visualPage = page;
        return true;
    }

    /**
     * Swaps pages 0 and 1: the page that was drawn on becomes the displayed one
     * (same behavior as WinBGI's {@code swapbuffers}).
     */
    public void swap() {
        if (activePage == 0) {
            visualPage = 0;
            activePage = 1;
        } else {
            visualPage = 1;
            activePage = 0;
        }
    }

    private static boolean isValid(int page) {
        return page >= 0 && page < MAX_PAGES;
    }
}
