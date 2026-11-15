package com.juniverse.graphics.driver;

/**
 * Motivo por el que {@link ModeResolver} no pudo resolver un modo gráfico.
 */
public enum ModeFailureReason {
    /** El código de controlador no existe (grInvalidDriver). */
    INVALID_DRIVER,
    /** El controlador no tiene ese modo (grInvalidMode). */
    INVALID_MODE
}
