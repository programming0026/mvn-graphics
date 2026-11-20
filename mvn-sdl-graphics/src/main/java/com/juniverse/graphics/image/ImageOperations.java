package com.juniverse.graphics.image;

import com.juniverse.graphics.style.RasterOperation;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Image capture, paste and file operations.
 */
public interface ImageOperations {

    /** Value meaning "up to the edge of the image or the screen". */
    int TO_EDGE = Integer.MAX_VALUE;

    /** Captures a rectangular region of the viewport. */
    BgiImage getImage(int left, int top, int right, int bottom);

    /** Pastes an image with its top-left corner at (left, top). */
    void putImage(int left, int top, BgiImage image, RasterOperation operation);

    /**
     * Reads an image file and draws it fitted to the given rectangle.
     * If {@code right} or {@code bottom} are {@link #TO_EDGE}, the
     * natural size of the image is used.
     */
    void readImageFile(Path file, int left, int top, int right, int bottom) throws IOException;

    /**
     * Saves the given region to a file. If {@code right} or
     * {@code bottom} are {@link #TO_EDGE}, the viewport edge is used.
     */
    void writeImageFile(Path file, int left, int top, int right, int bottom) throws IOException;
}
