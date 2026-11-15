package com.juniverse.graphics.driver;

import com.juniverse.graphics.dto.GraphicsMode;
import com.juniverse.graphics.dto.ModeResolution;

/**
 * Traduce los parámetros de {@code initgraph(&gd, &gm, path)} a un modo
 * gráfico concreto, aplicando las mismas reglas que Borland BGI.
 */
public final class ModeResolver {

    /** Modo que devuelve la autodetección: VGA en VGAHI (640x480). */
    public static final GraphicsMode DETECTED = GraphicsDriver.VGA.highestMode().orElseThrow();

    private ModeResolver() {
    }

    /**
     * Resuelve el modo solicitado.
     * <ul>
     *     <li>Con {@code DETECT} el modo se ignora y se elige VGAHI, igual que en
     *     BGI (por eso {@code gd = DETECT, gm = VGA} funciona).</li>
     *     <li>Un controlador desconocido produce {@link ModeFailureReason#INVALID_DRIVER}.</li>
     *     <li>Un modo que el controlador no tiene produce {@link ModeFailureReason#INVALID_MODE}.</li>
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
