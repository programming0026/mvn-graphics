package com.juniverse.graphics.input;

/**
 * Tipos de evento de mouse de WinBGI.
 * <p>
 * Los códigos coinciden con los mensajes de Windows que usaba WinBGI
 * ({@code WM_MOUSEMOVE}, {@code WM_LBUTTONDOWN}, ...), para facilitar la
 * migración de programas existentes.
 */
public enum MouseEventKind {
    MOVE(0x0200),
    LEFT_BUTTON_DOWN(0x0201),
    LEFT_BUTTON_UP(0x0202),
    LEFT_BUTTON_DOUBLE_CLICK(0x0203),
    RIGHT_BUTTON_DOWN(0x0204),
    RIGHT_BUTTON_UP(0x0205),
    RIGHT_BUTTON_DOUBLE_CLICK(0x0206),
    MIDDLE_BUTTON_DOWN(0x0207),
    MIDDLE_BUTTON_UP(0x0208),
    MIDDLE_BUTTON_DOUBLE_CLICK(0x0209);

    private final int code;

    private MouseEventKind(int code) {
        this.code = code;
    }

    /** Código numérico equivalente al mensaje de Windows. */
    public int code() {
        return code;
    }

    /** Obtiene el tipo de evento a partir de su código. */
    public static MouseEventKind fromCode(int code) {
        for (MouseEventKind kind : values()) {
            if (kind.code == code) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Invalid mouse event kind: " + code);
    }

    /** Evento de botón presionado para el botón indicado. */
    public static MouseEventKind down(MouseButton button) {
        return switch (button) {
            case LEFT -> LEFT_BUTTON_DOWN;
            case RIGHT -> RIGHT_BUTTON_DOWN;
            case MIDDLE -> MIDDLE_BUTTON_DOWN;
        };
    }

    /** Evento de botón liberado para el botón indicado. */
    public static MouseEventKind up(MouseButton button) {
        return switch (button) {
            case LEFT -> LEFT_BUTTON_UP;
            case RIGHT -> RIGHT_BUTTON_UP;
            case MIDDLE -> MIDDLE_BUTTON_UP;
        };
    }

    /** Evento de doble clic para el botón indicado. */
    public static MouseEventKind doubleClick(MouseButton button) {
        return switch (button) {
            case LEFT -> LEFT_BUTTON_DOUBLE_CLICK;
            case RIGHT -> RIGHT_BUTTON_DOUBLE_CLICK;
            case MIDDLE -> MIDDLE_BUTTON_DOUBLE_CLICK;
        };
    }
}
