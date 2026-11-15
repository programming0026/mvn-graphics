package com.juniverse.graphics.raster;

import com.juniverse.graphics.dto.Rect;
import com.juniverse.graphics.style.RasterOperation;

/**
 * Superficie de dibujo con coordenadas relativas al viewport activo.
 * <p>
 * Es la abstracción de la que dependen todos los dibujantes de alto nivel
 * (principio de inversión de dependencias): no conocen páginas, viewports ni
 * recortes, solo trazan y leen píxeles.
 */
public interface Canvas {

    /** Valor devuelto por {@link #read} para coordenadas fuera del área dibujable. */
    int OUTSIDE = 0x00000000;

    /** Ancho del área de dibujo (viewport). */
    int width();

    /** Alto del área de dibujo (viewport). */
    int height();

    /**
     * Rectángulo (en coordenadas relativas al viewport) que contiene todos los
     * puntos dibujables. Si el viewport no recorta, abarca la página completa.
     */
    Rect drawableBounds();

    /** Indica si un punto relativo al viewport puede dibujarse. */
    boolean isDrawable(int x, int y);

    /** Dibuja un píxel ARGB combinándolo con la operación indicada. */
    void plot(int x, int y, int argb, RasterOperation operation);

    /** Lee un píxel ARGB, o {@link #OUTSIDE} si la coordenada no es dibujable. */
    int read(int x, int y);
}
