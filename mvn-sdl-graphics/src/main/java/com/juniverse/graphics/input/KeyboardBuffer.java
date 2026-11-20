package com.juniverse.graphics.input;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * Queue of pending keys of a window (equivalent to {@code kbd_queue}).
 */
public final class KeyboardBuffer {

    /** Prefix that precedes an extended key. */
    public static final int EXTENDED_PREFIX = 0;

    private final Deque<Integer> keys = new ArrayDeque<>();

    /** Adds a character or control code (for example 13 for Enter). */
    public void push(int code) {
        keys.addLast(code);
    }

    /** Adds an extended key as the sequence 0, code. */
    public void pushExtended(ExtendedKey key) {
        keys.addLast(EXTENDED_PREFIX);
        keys.addLast(key.code());
    }

    /** Tells whether there are pending keys (equivalent to {@code kbhit}). */
    public boolean hasKey() {
        return !keys.isEmpty();
    }

    /**
     * Takes the next key.
     *
     * @throws NoSuchElementException if the queue is empty
     */
    public int poll() {
        Integer code = keys.pollFirst();
        if (code == null) {
            throw new NoSuchElementException("No pending keys");
        }
        return code;
    }

    /** Discards all pending keys. */
    public void clear() {
        keys.clear();
    }
}
