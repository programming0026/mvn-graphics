package com.juniverse.graphics.dto;

/**
 * Área de dibujo activa (equivalente a {@code viewporttype}).
 * <p>
 * Todas las coordenadas de dibujo son relativas a la esquina superior
 * izquierda del viewport. Si {@code clip} es verdadero, lo que quede fuera
 * del viewport no se dibuja.
 */
public record Viewport(int left, int top, int right, int bottom, boolean clip) {

    /** Crea un viewport validando que las coordenadas estén ordenadas. */
    public Viewport {
        if (right < left || bottom < top) {
            throw new IllegalArgumentException("Invalid viewport: (" + left + "," + top
                    + ")-(" + right + "," + bottom + ")");
        }
    }

    /** Viewport que cubre una ventana completa sin recorte. */
    public static Viewport fullScreen(int width, int height) {
        return new Viewport(0, 0, width - 1, height - 1, false);
    }

    /** Ancho del viewport en píxeles. */
    public int width() {
        return right - left + 1;
    }

    /** Alto del viewport en píxeles. */
    public int height() {
        return bottom - top + 1;
    }

    /** Indica si un punto absoluto de pantalla cae dentro del viewport. */
    public boolean containsAbsolute(int x, int y) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }
}
