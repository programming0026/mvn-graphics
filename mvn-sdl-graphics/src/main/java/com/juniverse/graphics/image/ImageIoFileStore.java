package com.juniverse.graphics.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Implementation of {@link ImageFileStore} based on {@code javax.imageio}.
 * Supports BMP, PNG, JPG and GIF.
 */
public final class ImageIoFileStore implements ImageFileStore {

    private static final String DEFAULT_FORMAT = "bmp";

    static {
        if (System.getProperty("java.awt.headless") == null) {
            System.setProperty("java.awt.headless", "true");
        }
    }

    @Override
    public BgiImage read(Path file) throws IOException {
        BufferedImage source = ImageIO.read(file.toFile());
        if (source == null) {
            throw new IOException("Unrecognized image format: " + file);
        }
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = source.getRGB(0, 0, width, height, null, 0, width);
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] |= 0xFF000000;
        }
        return new BgiImage(width, height, pixels);
    }

    @Override
    public void write(Path file, BgiImage image) throws IOException {
        BufferedImage target = new BufferedImage(image.width(), image.height(), BufferedImage.TYPE_INT_RGB);
        target.setRGB(0, 0, image.width(), image.height(), image.pixels(), 0, image.width());
        String format = formatOf(file);
        if (!ImageIO.write(target, format, file.toFile())) {
            throw new IOException("No image writer available for format: " + format);
        }
    }

    private static String formatOf(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return DEFAULT_FORMAT;
        }
        String extension = name.substring(dot + 1).toLowerCase(Locale.ROOT);
        return extension.equals("jpeg") ? "jpg" : extension;
    }
}
