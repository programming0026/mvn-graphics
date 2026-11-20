package com.juniverse.graphics.dto;

import com.juniverse.graphics.driver.ModeFailureReason;

/**
 * Result of resolving the {@code initGraph} parameters: a valid graphics mode
 * or the reason why it could not be resolved.
 */
public sealed interface ModeResolution {

    /** The driver and the mode are valid. */
    public record Resolved(GraphicsMode mode) implements ModeResolution {
    }

    /** The driver or the mode are not valid. */
    public record Failed(ModeFailureReason reason) implements ModeResolution {
    }
}
