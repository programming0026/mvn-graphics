package com.juniverse.graphics.image;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Abstraction to read and write images on disk.
 * <p>
 * Decouples the image operations from the concrete encoding
 * library (ImageIO, SDL_image, etc.).
 */
public interface ImageFileStore {

    /** Reads an image from a file. */
    BgiImage read(Path file) throws IOException;

    /** Writes an image to a file; the format is deduced from the extension. */
    void write(Path file, BgiImage image) throws IOException;
}
