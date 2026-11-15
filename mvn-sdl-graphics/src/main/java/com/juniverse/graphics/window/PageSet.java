package com.juniverse.graphics.window;

import com.juniverse.graphics.raster.FrameBuffer;

/**
 * Conjunto de páginas de una ventana con la página activa (donde se dibuja)
 * y la página visual (la que se muestra).
 */
public final class PageSet {

    /** Número máximo de páginas (igual que WinBGI). */
    public static final int MAX_PAGES = 4;

    private final FrameBuffer[] pages = new FrameBuffer[MAX_PAGES];
    private int activePage;
    private int visualPage;

    /**
     * Crea las páginas rellenas con el color de fondo inicial.
     *
     * @param backgroundArgb color ARGB opaco con el que se inicializan las páginas
     */
    public PageSet(int width, int height, int backgroundArgb) {
        for (int i = 0; i < MAX_PAGES; i++) {
            pages[i] = new FrameBuffer(width, height);
            pages[i].fill(backgroundArgb);
        }
    }

    /**
     * Garantiza que las páginas midan al menos el tamaño indicado. Las páginas
     * solo crecen, nunca se achican: así, si la ventana se reduce o se
     * minimiza, lo dibujado fuera de la parte visible no se pierde. El
     * contenido se conserva anclado arriba a la izquierda y el área nueva se
     * rellena con el color indicado.
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

    /** Rellena todas las páginas con un color. */
    public void clearAll(int argb) {
        for (FrameBuffer page : pages) {
            page.fill(argb);
        }
    }

    /** Ancho real en memoria de las páginas. */
    public int capacityWidth() {
        return pages[0].width();
    }

    /** Alto real en memoria de las páginas. */
    public int capacityHeight() {
        return pages[0].height();
    }

    /** Página donde se dibuja actualmente. */
    public FrameBuffer active() {
        return pages[activePage];
    }

    /** Página que se muestra en pantalla. */
    public FrameBuffer visual() {
        return pages[visualPage];
    }

    public int activeIndex() {
        return activePage;
    }

    public int visualIndex() {
        return visualPage;
    }

    /** Indica si se está mostrando la misma página en la que se dibuja. */
    public boolean isViewingActivePage() {
        return activePage == visualPage;
    }

    /**
     * Cambia la página activa.
     *
     * @return falso si el número de página no es válido
     */
    public boolean setActive(int page) {
        if (!isValid(page)) {
            return false;
        }
        activePage = page;
        return true;
    }

    /**
     * Cambia la página visual.
     *
     * @return falso si el número de página no es válido
     */
    public boolean setVisual(int page) {
        if (!isValid(page)) {
            return false;
        }
        visualPage = page;
        return true;
    }

    /**
     * Alterna las páginas 0 y 1: la página en la que se dibujó pasa a mostrarse
     * (mismo comportamiento que {@code swapbuffers} de WinBGI).
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
