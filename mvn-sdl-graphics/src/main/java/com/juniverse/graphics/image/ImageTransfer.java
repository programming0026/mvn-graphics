package com.juniverse.graphics.image;

import com.juniverse.graphics.color.ColorCode;
import com.juniverse.graphics.drawing.DrawingContext;
import com.juniverse.graphics.dto.Rect;
import com.juniverse.graphics.raster.Canvas;
import com.juniverse.graphics.style.RasterOperation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Implementación de {@link ImageOperations} sobre el lienzo activo.
 */
public final class ImageTransfer implements ImageOperations {

    private final DrawingContext context;
    private final ImageFileStore fileStore;

    public ImageTransfer(DrawingContext context, ImageFileStore fileStore) {
        this.context = Objects.requireNonNull(context);
        this.fileStore = Objects.requireNonNull(fileStore);
    }

    @Override
    public BgiImage getImage(int left, int top, int right, int bottom) {
        Rect area = new Rect(left, top, right, bottom).normalized();
        Canvas canvas = context.canvas();
        int[] pixels = new int[area.width() * area.height()];
        int index = 0;
        for (int y = area.top(); y <= area.bottom(); y++) {
            for (int x = area.left(); x <= area.right(); x++) {
                int argb = canvas.read(x, y);
                pixels[index++] = argb == Canvas.OUTSIDE ? ColorCode.OPAQUE : argb;
            }
        }
        return new BgiImage(area.width(), area.height(), pixels);
    }

    @Override
    public void putImage(int left, int top, BgiImage image, RasterOperation operation) {
        Objects.requireNonNull(image);
        Canvas canvas = context.canvas();
        for (int y = 0; y < image.height(); y++) {
            for (int x = 0; x < image.width(); x++) {
                canvas.plot(left + x, top + y, image.pixel(x, y), operation);
            }
        }
        context.changed();
    }

    @Override
    public void readImageFile(Path file, int left, int top, int right, int bottom) throws IOException {
        BgiImage image = fileStore.read(file);
        int width = right == TO_EDGE ? image.width() : Math.abs(right - left) + 1;
        int height = bottom == TO_EDGE ? image.height() : Math.abs(bottom - top) + 1;
        putImage(Math.min(left, right), Math.min(top, bottom), image.scaledTo(width, height), RasterOperation.COPY);
    }

    @Override
    public void writeImageFile(Path file, int left, int top, int right, int bottom) throws IOException {
        int effectiveRight = right == TO_EDGE ? context.canvas().width() - 1 : right;
        int effectiveBottom = bottom == TO_EDGE ? context.canvas().height() - 1 : bottom;
        fileStore.write(file, getImage(left, top, effectiveRight, effectiveBottom));
    }
}
