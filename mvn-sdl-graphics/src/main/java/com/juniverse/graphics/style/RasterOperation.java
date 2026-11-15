package com.juniverse.graphics.style;

import com.juniverse.graphics.color.ColorCode;

/**
 * Operaciones de combinación de píxeles usadas por {@code setwritemode} y
 * {@code putimage} ({@code putimage_ops}).
 * <p>
 * Cada operación combina el píxel destino con el píxel fuente sobre los 24
 * bits de color y conserva el canal alfa opaco.
 */
public enum RasterOperation {
    COPY {
        @Override
        protected int combine(int destination, int source) {
            return source;
        }
    },
    XOR {
        @Override
        protected int combine(int destination, int source) {
            return destination ^ source;
        }
    },
    OR {
        @Override
        protected int combine(int destination, int source) {
            return destination | source;
        }
    },
    AND {
        @Override
        protected int combine(int destination, int source) {
            return destination & source;
        }
    },
    NOT {
        @Override
        protected int combine(int destination, int source) {
            return ~source;
        }
    };

    /** Combina dos píxeles ARGB y devuelve el resultado opaco. */
    public int apply(int destination, int source) {
        return ColorCode.OPAQUE | (combine(destination, source) & ColorCode.RGB_MASK);
    }

    protected abstract int combine(int destination, int source);

    /** Obtiene la operación a partir de su código numérico BGI. */
    public static RasterOperation fromCode(int code) {
        if (code < 0 || code >= values().length) {
            throw new IllegalArgumentException("Invalid write mode: " + code);
        }
        return values()[code];
    }
}
