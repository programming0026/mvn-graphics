package com.juniverse.graphics.driver;

import com.juniverse.graphics.dto.GraphicsMode;
import com.juniverse.graphics.dto.ModeResolution;

/**
 * Translates the parameters of {@code initgraph(&gd, &gm, path)} to a specific
 * graphics mode, applying the same rules as Borland BGI.
 */
public final class ModeResolver {

    /** Mode returned by autodetection: VGA in VGAHI (640x480). */
    public static final GraphicsMode DETECTED = GraphicsDriver.VGA.highestMode().orElseThrow();

    private ModeResolver() {
    }

    /**
     * Resolves the requested mode.
     * <ul>
     *     <li>With {@code DETECT} the mode is ignored and VGAHI is chosen, just as in
     *     BGI (that is why {@code gd = DETECT, gm = VGA} works).</li>
     *     <li>An unknown driver produces {@link ModeFailureReason#INVALID_DRIVER}.</li>
     *     <li>A mode the driver does not have produces {@link ModeFailureReason#INVALID_MODE}.</li>
     * </ul>
     */
    public static ModeResolution resolve(int driverCode, int modeNumber) {
        if (driverCode == GraphicsDriver.DETECT.code()) {
            return new ModeResolution.Resolved(DETECTED);
        }
        return GraphicsDriver.fromCode(driverCode)
                .<ModeResolution>map(driver -> driver.mode(modeNumber)
                        .<ModeResolution>map(ModeResolution.Resolved::new)
                        .orElse(new ModeResolution.Failed(ModeFailureReason.INVALID_MODE)))
                .orElse(new ModeResolution.Failed(ModeFailureReason.INVALID_DRIVER));
    }
}
