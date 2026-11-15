package com.juniverse.graphics.dto;

import com.juniverse.graphics.driver.ModeFailureReason;

/**
 * Resultado de resolver los parámetros de {@code initGraph}: un modo gráfico
 * válido o el motivo por el que no se pudo resolver.
 */
public sealed interface ModeResolution {

    /** El controlador y el modo son válidos. */
    public record Resolved(GraphicsMode mode) implements ModeResolution {
    }

    /** El controlador o el modo no son válidos. */
    public record Failed(ModeFailureReason reason) implements ModeResolution {
    }
}
