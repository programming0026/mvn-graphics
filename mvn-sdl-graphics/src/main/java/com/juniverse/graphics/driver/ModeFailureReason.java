package com.juniverse.graphics.driver;

/**
 * Reason why {@link ModeResolver} could not resolve a graphics mode.
 */
public enum ModeFailureReason {
    /** The driver code does not exist (grInvalidDriver). */
    INVALID_DRIVER,
    /** The driver does not have that mode (grInvalidMode). */
    INVALID_MODE
}
