package com.juniverse.graphics.input;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * Cola de teclas pendientes de una ventana (equivale a {@code kbd_queue}).
 */
public final class KeyboardBuffer {

    /** Prefijo que antecede a una tecla extendida. */
    public static final int EXTENDED_PREFIX = 0;

    private final Deque<Integer> keys = new ArrayDeque<>();

    /** Agrega un carácter o código de control (por ejemplo 13 para Enter). */
    public void push(int code) {
        keys.addLast(code);
    }

    /** Agrega una tecla extendida como la secuencia 0, código. */
    public void pushExtended(ExtendedKey key) {
        keys.addLast(EXTENDED_PREFIX);
        keys.addLast(key.code());
    }

    /** Indica si hay teclas pendientes (equivale a {@code kbhit}). */
    public boolean hasKey() {
        return !keys.isEmpty();
    }

    /**
     * Extrae la siguiente tecla.
     *
     * @throws NoSuchElementException si la cola está vacía
     */
    public int poll() {
        Integer code = keys.pollFirst();
        if (code == null) {
            throw new NoSuchElementException("No pending keys");
        }
        return code;
    }

    /** Descarta todas las teclas pendientes. */
    public void clear() {
        keys.clear();
    }
}
