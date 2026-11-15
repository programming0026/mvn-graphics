package com.juniverse.graphics.image;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Abstracción para leer y escribir imágenes en disco.
 * <p>
 * Desacopla las operaciones de imagen de la biblioteca concreta de
 * codificación (ImageIO, SDL_image, etc.).
 */
public interface ImageFileStore {

    /** Lee una imagen desde un archivo. */
    BgiImage read(Path file) throws IOException;

    /** Escribe una imagen en un archivo; el formato se deduce de la extensión. */
    void write(Path file, BgiImage image) throws IOException;
}
