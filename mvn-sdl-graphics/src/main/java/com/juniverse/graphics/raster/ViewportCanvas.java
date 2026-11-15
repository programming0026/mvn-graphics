package com.juniverse.graphics.raster;

import com.juniverse.graphics.dto.Rect;
import com.juniverse.graphics.dto.Viewport;
import com.juniverse.graphics.style.RasterOperation;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Implementación de {@link Canvas} que traslada las coordenadas al viewport
 * actual y aplica el recorte sobre la página activa.
 * <p>
 * La página y el viewport se obtienen mediante proveedores para que el lienzo
 * siempre dibuje sobre la página activa vigente sin necesidad de recrearlo.
 */
public final class ViewportCanvas implements Canvas {

    private final Supplier<FrameBuffer> target;
    private final Supplier<Viewport> viewport;

    public ViewportCanvas(Supplier<FrameBuffer> target, Supplier<Viewport> viewport) {
        this.target = Objects.requireNonNull(target);
        this.viewport = Objects.requireNonNull(viewport);
    }

    @Override
    public int width() {
        return viewport.get().width();
    }

    @Override
    public int height() {
        return viewport.get().height();
    }

    @Override
    public Rect drawableBounds() {
        Viewport vp = viewport.get();
        FrameBuffer page = target.get();
        int left = -vp.left();
        int top = -vp.top();
        int right = page.width() - 1 - vp.left();
        int bottom = page.height() - 1 - vp.top();
        if (vp.clip()) {
            left = Math.max(left, 0);
            top = Math.max(top, 0);
            right = Math.min(right, vp.width() - 1);
            bottom = Math.min(bottom, vp.height() - 1);
        }
        return new Rect(left, top, right, bottom);
    }

    @Override
    public boolean isDrawable(int x, int y) {
        Viewport vp = viewport.get();
        int ax = vp.left() + x;
        int ay = vp.top() + y;
        if (vp.clip() && !vp.containsAbsolute(ax, ay)) {
            return false;
        }
        return target.get().contains(ax, ay);
    }

    @Override
    public void plot(int x, int y, int argb, RasterOperation operation) {
        if (!isDrawable(x, y)) {
            return;
        }
        Viewport vp = viewport.get();
        FrameBuffer page = target.get();
        int ax = vp.left() + x;
        int ay = vp.top() + y;
        page.set(ax, ay, operation.apply(page.get(ax, ay), argb));
    }

    @Override
    public int read(int x, int y) {
        if (!isDrawable(x, y)) {
            return OUTSIDE;
        }
        Viewport vp = viewport.get();
        return target.get().get(vp.left() + x, vp.top() + y);
    }
}
