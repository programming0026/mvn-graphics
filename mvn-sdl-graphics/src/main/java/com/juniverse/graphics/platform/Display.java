package com.juniverse.graphics.platform;

import com.juniverse.graphics.dto.Size;
import com.juniverse.graphics.raster.FrameBuffer;

/**
 * Ventana nativa capaz de mostrar el contenido de una página.
 */
public interface Display extends AutoCloseable {

    /** Identificador nativo con el que la plataforma etiqueta los eventos. */
    int id();

    /**
     * Muestra en pantalla la región superior izquierda de la página. Si el
     * tamaño de la región cambió desde la última vez, la ventana se adapta.
     *
     * @param page       página a mostrar
     * @param area       región visible (puede ser menor que la página)
     * @param borderArgb color ARGB del área de la ventana que no cubre la página
     */
    void present(FrameBuffer page, Size area, int borderArgb);

    /** Tamaño real en píxeles del área cliente de la ventana. */
    Size outputSize();

    /** Cambia la forma en que la página se ajusta a la ventana. */
    void setScalingMode(ScalingMode mode);

    /** Cambia el título de la ventana. */
    void setTitle(String title);

    /** Libera los recursos nativos de la ventana. */
    @Override
    void close();
}
