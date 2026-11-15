package com.juniverse.graphics.window;

/**
 * Códigos de error de BGI ({@code graph_errors}) con sus mensajes.
 */
public enum GraphicsError {
    OK(0, "No error"),
    NO_INIT_GRAPH(-1, "(BGI) graphics not installed (use initgraph)"),
    NOT_DETECTED(-2, "Graphics hardware not detected"),
    FILE_NOT_FOUND(-3, "Device driver file not found"),
    INVALID_DRIVER(-4, "Invalid device driver file"),
    NO_LOAD_MEM(-5, "Not enough memory to load driver"),
    NO_SCAN_MEM(-6, "Out of memory in scan fill"),
    NO_FLOOD_MEM(-7, "Out of memory in flood fill"),
    FONT_NOT_FOUND(-8, "Font file not found"),
    NO_FONT_MEM(-9, "Not enough memory to load font"),
    INVALID_MODE(-10, "Invalid graphics mode for selected driver"),
    ERROR(-11, "Graphics error"),
    IO_ERROR(-12, "Graphics I/O error"),
    INVALID_FONT(-13, "Invalid font file"),
    INVALID_FONT_NUM(-14, "Invalid font number"),
    INVALID_DEVICE_NUM(-15, "Invalid device number"),
    INVALID_VERSION(-18, "Invalid version number");

    private final int code;
    private final String message;

    private GraphicsError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /** Código numérico BGI (0 = sin error, negativo = error). */
    public int code() {
        return code;
    }

    /** Mensaje descriptivo (equivale a {@code grapherrormsg}). */
    public String message() {
        return message;
    }

    /** Busca el error correspondiente a un código numérico. */
    public static GraphicsError fromCode(int code) {
        for (GraphicsError error : values()) {
            if (error.code == code) {
                return error;
            }
        }
        return ERROR;
    }
}
