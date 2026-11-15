package com.juniverse.graphics.image;

import com.juniverse.graphics.style.RasterOperation;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Operaciones de captura, pegado y archivos de imagen.
 */
public interface ImageOperations {

    /** Valor que indica "hasta el borde de la imagen o de la pantalla". */
    int TO_EDGE = Integer.MAX_VALUE;

    /** Captura una región rectangular del viewport. */
    BgiImage getImage(int left, int top, int right, int bottom);

    /** Pega una imagen con la esquina superior izquierda en (left, top). */
    void putImage(int left, int top, BgiImage image, RasterOperation operation);

    /**
     * Lee un archivo de imagen y lo dibuja ajustado al rectángulo indicado.
     * Si {@code right} o {@code bottom} valen {@link #TO_EDGE}, se usa el
     * tamaño natural de la imagen.
     */
    void readImageFile(Path file, int left, int top, int right, int bottom) throws IOException;

    /**
     * Guarda en un archivo la región indicada. Si {@code right} o
     * {@code bottom} valen {@link #TO_EDGE}, se usa el borde del viewport.
     */
    void writeImageFile(Path file, int left, int top, int right, int bottom) throws IOException;
}
