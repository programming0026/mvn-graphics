package com.juniverse.graphics.input;

/**
 * Extended BGI/conio keys.
 * <p>
 * When one of these keys is pressed, {@code getch()} first returns a
 * 0 and on the next call the key code, just as in DOS.
 */
public enum ExtendedKey {
    HOME(71),
    UP(72),
    PAGE_UP(73),
    LEFT(75),
    CENTER(76),
    RIGHT(77),
    END(79),
    DOWN(80),
    PAGE_DOWN(81),
    INSERT(82),
    DELETE(83),
    F1(59),
    F2(60),
    F3(61),
    F4(62),
    F5(63),
    F6(64),
    F7(65),
    F8(66),
    F9(67);

    private final int code;

    private ExtendedKey(int code) {
        this.code = code;
    }

    /** Code returned by {@code getch()} after the 0. */
    public int code() {
        return code;
    }
}
