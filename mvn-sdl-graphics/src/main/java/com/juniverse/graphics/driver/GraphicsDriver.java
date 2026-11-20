package com.juniverse.graphics.driver;

import com.juniverse.graphics.dto.GraphicsMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * BGI graphics drivers ({@code graphics_drivers}) with their modes.
 * <p>
 * There is no real CGA/EGA/VGA hardware: each driver only defines the
 * resolutions of its modes, which determine the size of the window that
 * {@code initGraph} opens.
 */
public enum GraphicsDriver {
    /** Autodetection: always selects VGA in its highest mode (VGAHI). */
    DETECT(0, "DETECT"),
    CGA(1, "CGA"),
    MCGA(2, "CGA"),
    EGA(3, "EGAVGA"),
    EGA64(4, "EGAVGA"),
    EGAMONO(5, "EGAVGA"),
    IBM8514(6, "IBM8514"),
    HERCMONO(7, "HERC"),
    ATT400(8, "ATT"),
    VGA(9, "EGAVGA"),
    PC3270(10, "PC3270");

    private final int code;
    private final String fileName;
    private List<GraphicsMode> modes = List.of();

    static {
        CGA.define(0, "CGAC0", 320, 200).define(1, "CGAC1", 320, 200).define(2, "CGAC2", 320, 200)
                .define(3, "CGAC3", 320, 200).define(4, "CGAHI", 640, 200);
        MCGA.define(0, "MCGAC0", 320, 200).define(1, "MCGAC1", 320, 200).define(2, "MCGAC2", 320, 200)
                .define(3, "MCGAC3", 320, 200).define(4, "MCGAMED", 640, 200).define(5, "MCGAHI", 640, 480);
        EGA.define(0, "EGALO", 640, 200).define(1, "EGAHI", 640, 350);
        EGA64.define(0, "EGA64LO", 640, 200).define(1, "EGA64HI", 640, 350);
        EGAMONO.define(3, "EGAMONOHI", 640, 350);
        IBM8514.define(0, "IBM8514LO", 640, 480).define(1, "IBM8514HI", 1024, 768);
        HERCMONO.define(0, "HERCMONOHI", 720, 348);
        ATT400.define(0, "ATT400C0", 320, 200).define(1, "ATT400C1", 320, 200).define(2, "ATT400C2", 320, 200)
                .define(3, "ATT400C3", 320, 200).define(4, "ATT400MED", 640, 200).define(5, "ATT400HI", 640, 400);
        VGA.define(0, "VGALO", 640, 200).define(1, "VGAMED", 640, 350).define(2, "VGAHI", 640, 480);
        PC3270.define(0, "PC3270HI", 720, 350);
    }

    private GraphicsDriver(int code, String fileName) {
        this.code = code;
        this.fileName = fileName;
    }

    private GraphicsDriver define(int number, String label, int width, int height) {
        List<GraphicsMode> extended = new ArrayList<>(modes);
        extended.add(new GraphicsMode(this, number, label, width, height));
        modes = Collections.unmodifiableList(extended);
        return this;
    }

    /** BGI numeric code of the driver. */
    public int code() {
        return code;
    }

    /** Name of the Borland driver file (equivalent to {@code getdrivername}). */
    public String fileName() {
        return fileName;
    }

    /** Modes of the driver, sorted by number. */
    public List<GraphicsMode> modes() {
        return modes;
    }

    /** Mode with the given number, if it exists. */
    public Optional<GraphicsMode> mode(int number) {
        return modes.stream().filter(mode -> mode.number() == number).findFirst();
    }

    /** Highest-resolution mode (the one chosen by autodetection). */
    public Optional<GraphicsMode> highestMode() {
        return modes.isEmpty() ? Optional.empty() : Optional.of(modes.get(modes.size() - 1));
    }

    /** Lowest mode number (equivalent to the {@code lomode} of {@code getmoderange}). */
    public int lowestModeNumber() {
        return modes.isEmpty() ? -1 : modes.get(0).number();
    }

    /** Highest mode number (equivalent to the {@code himode} of {@code getmoderange}). */
    public int highestModeNumber() {
        return modes.isEmpty() ? -1 : modes.get(modes.size() - 1).number();
    }

    /** Driver that corresponds to a BGI numeric code. */
    public static Optional<GraphicsDriver> fromCode(int code) {
        for (GraphicsDriver driver : values()) {
            if (driver.code == code) {
                return Optional.of(driver);
            }
        }
        return Optional.empty();
    }
}
