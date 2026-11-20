package com.juniverse.graphics.input;

/**
 * WinBGI mouse event kinds.
 * <p>
 * The codes match the Windows messages that WinBGI used
 * ({@code WM_MOUSEMOVE}, {@code WM_LBUTTONDOWN}, ...), to make it easier to
 * migrate existing programs.
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

    /** Numeric code equivalent to the Windows message. */
    public int code() {
        return code;
    }

    /** Gets the event kind from its code. */
    public static MouseEventKind fromCode(int code) {
        for (MouseEventKind kind : values()) {
            if (kind.code == code) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Invalid mouse event kind: " + code);
    }

    /** Button-pressed event for the given button. */
    public static MouseEventKind down(MouseButton button) {
        return switch (button) {
            case LEFT -> LEFT_BUTTON_DOWN;
            case RIGHT -> RIGHT_BUTTON_DOWN;
            case MIDDLE -> MIDDLE_BUTTON_DOWN;
        };
    }

    /** Button-released event for the given button. */
    public static MouseEventKind up(MouseButton button) {
        return switch (button) {
            case LEFT -> LEFT_BUTTON_UP;
            case RIGHT -> RIGHT_BUTTON_UP;
            case MIDDLE -> MIDDLE_BUTTON_UP;
        };
    }

    /** Double-click event for the given button. */
    public static MouseEventKind doubleClick(MouseButton button) {
        return switch (button) {
            case LEFT -> LEFT_BUTTON_DOUBLE_CLICK;
            case RIGHT -> RIGHT_BUTTON_DOUBLE_CLICK;
            case MIDDLE -> MIDDLE_BUTTON_DOUBLE_CLICK;
        };
    }
}
