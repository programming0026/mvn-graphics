package com.juniverse.graphics.text;

/**
 * Justificación vertical del texto (BOTTOM_TEXT, VCENTER_TEXT, TOP_TEXT).
 * <p>
 * El orden de las constantes respeta los códigos numéricos de BGI.
 */
public enum VerticalAlignment {
    BOTTOM,
    CENTER,
    TOP;

    /** Desplazamiento a aplicar al origen para un texto de alto {@code height}. */
    public int offset(int height) {
        return switch (this) {
            case TOP -> 0;
            case CENTER -> -height / 2;
            case BOTTOM -> -height;
        };
    }
}
