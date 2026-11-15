package com.juniverse.graphics.examples.firedemo;

import com.juniverse.graphics.image.BgiImage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FireSimulationTest {

    private static final int BLACK = 0xFF000000;

    @Test
    public void fireRisesFromBottomAndTopRowsStayBlack() {
        FireEffect.FireSimulation fire = new FireEffect.FireSimulation(new Random(42));
        for (int frame = 0; frame < 100; frame++) {
            fire.step();
        }
        int[] palette = new int[256];
        Arrays.fill(palette, 0xFFFFFFFF);
        palette[0] = BLACK;
        BgiImage image = fire.render(palette);

        assertEquals(FireEffect.FireSimulation.WIDTH, image.width());
        assertEquals(FireEffect.FireSimulation.HEIGHT, image.height());
        // Las filas superiores al área simulada nunca se calientan.
        for (int x = 0; x < image.width(); x++) {
            assertEquals(BLACK, image.pixel(x, 0));
        }
        // En las filas inferiores ya hay fuego.
        boolean burning = false;
        for (int x = 0; x < image.width(); x++) {
            burning |= image.pixel(x, image.height() - 1) != BLACK;
        }
        assertTrue(burning);
    }
}
