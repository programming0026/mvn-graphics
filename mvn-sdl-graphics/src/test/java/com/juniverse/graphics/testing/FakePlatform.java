package com.juniverse.graphics.testing;

import com.juniverse.graphics.dto.Size;
import com.juniverse.graphics.dto.InputEvent;
import com.juniverse.graphics.platform.Display;
import com.juniverse.graphics.dto.DisplaySpec;
import com.juniverse.graphics.platform.Platform;
import com.juniverse.graphics.platform.ScalingMode;
import com.juniverse.graphics.dto.ScreenSize;
import com.juniverse.graphics.raster.FrameBuffer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

/**
 * Plataforma en memoria para pruebas: no abre ventanas reales y permite
 * inyectar eventos de entrada.
 */
public final class FakePlatform implements Platform {

    private final Deque<InputEvent> pendingEvents = new ArrayDeque<>();
    private final List<FakeDisplay> displays = new ArrayList<>();
    private boolean closed;

    @Override
    public Display openDisplay(DisplaySpec spec) {
        FakeDisplay display = new FakeDisplay(displays.size() + 1, new Size(spec.width(), spec.height()));
        displays.add(display);
        return display;
    }

    @Override
    public void pollEvents(Consumer<InputEvent> consumer) {
        while (!pendingEvents.isEmpty()) {
            consumer.accept(pendingEvents.poll());
        }
    }

    @Override
    public ScreenSize screenSize() {
        return new ScreenSize(1920, 1080);
    }

    @Override
    public void showErrorMessage(String title, String message) {
    }

    @Override
    public void close() {
        closed = true;
    }

    /** Encola un evento que se entregará en el próximo sondeo. */
    public void enqueue(InputEvent event) {
        pendingEvents.add(event);
    }

    public List<FakeDisplay> displays() {
        return displays;
    }

    public boolean isClosed() {
        return closed;
    }

    /** Ventana falsa que cuenta las presentaciones. */
    public static final class FakeDisplay implements Display {
        private final int id;
        private int presentCount;
        private int lastBorderArgb;
        private ScalingMode scalingMode = ScalingMode.RESIZE;
        private Size outputSize;
        private Size lastPageSize;
        private boolean closed;

        private FakeDisplay(int id, Size outputSize) {
            this.id = id;
            this.outputSize = outputSize;
        }

        @Override
        public Size outputSize() {
            return outputSize;
        }

        /** Simula que el usuario cambió el tamaño de la ventana. */
        public void setOutputSize(Size outputSize) {
            this.outputSize = outputSize;
        }

        /** Tamaño de la última página presentada. */
        public Size lastPageSize() {
            return lastPageSize;
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public void present(FrameBuffer page, Size area, int borderArgb) {
            presentCount++;
            lastBorderArgb = borderArgb;
            lastPageSize = area;
        }

        @Override
        public void setScalingMode(ScalingMode mode) {
            scalingMode = mode;
        }

        public int lastBorderArgb() {
            return lastBorderArgb;
        }

        public ScalingMode scalingMode() {
            return scalingMode;
        }

        @Override
        public void setTitle(String title) {
        }

        @Override
        public void close() {
            closed = true;
        }

        public int presentCount() {
            return presentCount;
        }

        public boolean isClosed() {
            return closed;
        }
    }
}
