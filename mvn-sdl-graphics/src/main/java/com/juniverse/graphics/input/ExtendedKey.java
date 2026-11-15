package com.juniverse.graphics.input;

/**
 * Teclas extendidas de BGI/conio.
 * <p>
 * Cuando se presiona una de estas teclas, {@code getch()} devuelve primero un
 * 0 y en la siguiente llamada el código de la tecla, igual que en DOS.
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

    /** Código que devuelve {@code getch()} después del 0. */
    public int code() {
        return code;
    }
}
