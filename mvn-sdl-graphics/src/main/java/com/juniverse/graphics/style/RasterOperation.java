package com.juniverse.graphics.style;

import com.juniverse.graphics.color.ColorCode;

/**
 * Pixel combination operations used by {@code setwritemode} and
 * {@code putimage} ({@code putimage_ops}).
 * <p>
 * Each operation combines the destination pixel with the source pixel over the 24
 * color bits and keeps the alpha channel opaque.
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

    /** Combines two ARGB pixels and returns the opaque result. */
    public int apply(int destination, int source) {
        return ColorCode.OPAQUE | (combine(destination, source) & ColorCode.RGB_MASK);
    }

    protected abstract int combine(int destination, int source);

    /** Gets the operation from its BGI numeric code. */
    public static RasterOperation fromCode(int code) {
        if (code < 0 || code >= values().length) {
            throw new IllegalArgumentException("Invalid write mode: " + code);
        }
        return values()[code];
    }
}
