package com.juniverse.graphics.platform.sdl;

import com.juniverse.graphics.dto.Size;
import com.juniverse.graphics.platform.Display;
import com.juniverse.graphics.dto.DisplaySpec;
import com.juniverse.graphics.exception.PlatformException;
import com.juniverse.graphics.platform.ScalingMode;
import com.juniverse.graphics.raster.FrameBuffer;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_Rect;
import org.lwjgl.sdl.SDL_Texture;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

import static org.lwjgl.sdl.SDLError.SDL_GetError;
import static org.lwjgl.sdl.SDLKeyboard.SDL_StartTextInput;
import static org.lwjgl.sdl.SDLPixels.SDL_PIXELFORMAT_ARGB8888;
import static org.lwjgl.sdl.SDLRender.SDL_ConvertEventToRenderCoordinates;
import static org.lwjgl.sdl.SDLRender.SDL_CreateTexture;
import static org.lwjgl.sdl.SDLRender.SDL_DestroyRenderer;
import static org.lwjgl.sdl.SDLRender.SDL_DestroyTexture;
import static org.lwjgl.sdl.SDLRender.SDL_GetRenderOutputSize;
import static org.lwjgl.sdl.SDLRender.SDL_LOGICAL_PRESENTATION_INTEGER_SCALE;
import static org.lwjgl.sdl.SDLRender.SDL_LOGICAL_PRESENTATION_LETTERBOX;
import static org.lwjgl.sdl.SDLRender.SDL_LOGICAL_PRESENTATION_STRETCH;
import static org.lwjgl.sdl.SDLRender.SDL_RenderClear;
import static org.lwjgl.sdl.SDLRender.SDL_RenderPresent;
import static org.lwjgl.sdl.SDLRender.SDL_RenderTexture;
import static org.lwjgl.sdl.SDLRender.SDL_SetRenderDrawColor;
import static org.lwjgl.sdl.SDLRender.SDL_SetRenderLogicalPresentation;
import static org.lwjgl.sdl.SDLRender.SDL_SetTextureScaleMode;
import static org.lwjgl.sdl.SDLRender.SDL_TEXTUREACCESS_STREAMING;
import static org.lwjgl.sdl.SDLRender.SDL_UpdateTexture;
import static org.lwjgl.sdl.SDLRender.nSDL_CreateRenderer;
import static org.lwjgl.sdl.SDLSurface.SDL_SCALEMODE_LINEAR;
import static org.lwjgl.sdl.SDLSurface.SDL_SCALEMODE_NEAREST;
import static org.lwjgl.sdl.SDLVideo.SDL_CreateWindow;
import static org.lwjgl.sdl.SDLVideo.SDL_DestroyWindow;
import static org.lwjgl.sdl.SDLVideo.SDL_GetDisplayUsableBounds;
import static org.lwjgl.sdl.SDLVideo.SDL_GetPrimaryDisplay;
import static org.lwjgl.sdl.SDLVideo.SDL_GetWindowID;
import static org.lwjgl.sdl.SDLVideo.SDL_SetWindowMinimumSize;
import static org.lwjgl.sdl.SDLVideo.SDL_SetWindowPosition;
import static org.lwjgl.sdl.SDLVideo.SDL_SetWindowTitle;
import static org.lwjgl.sdl.SDLVideo.SDL_WINDOWPOS_CENTERED_MASK;
import static org.lwjgl.sdl.SDLVideo.SDL_WINDOW_RESIZABLE;

/**
 * SDL3 window that shows a page through a streaming texture.
 * <p>
 * The window is resizable and maximizable. Depending on the {@link ScalingMode},
 * the page grows with the window (RESIZE) or SDL scales it to the real size of
 * the window (keeping the aspect ratio, stretching it or by integer factors);
 * the leftover bands are painted with the background color. If the requested
 * resolution does not fit on the screen, the window opens shrunk.
 */
public final class SdlDisplay implements Display {

    /** Space reserved for the title bar and the window borders. */
    private static final int DECORATION_WIDTH = 16;
    private static final int DECORATION_HEIGHT = 64;
    /** Minimum window size when resizing it. */
    private static final int MIN_WINDOW_SIZE = 160;

    private final long window;
    private final long renderer;
    private final int id;
    private final Consumer<SdlDisplay> onClose;
    // The texture and the upload buffer are recreated if the page size changes.
    private SDL_Texture texture;
    private ByteBuffer upload;
    private IntBuffer uploadPixels;
    private int width;
    private int height;
    private ScalingMode scalingMode = ScalingMode.RESIZE;
    private int scaleMode = -1;
    private boolean closed;

    /**
     * @param spec    window parameters (the size is the BGI logical resolution)
     * @param onClose action to run on close (for example, removing it from the registry)
     */
    public SdlDisplay(DisplaySpec spec, Consumer<SdlDisplay> onClose) {
        this.width = spec.width();
        this.height = spec.height();
        this.onClose = onClose;

        int[] windowSize = fitToScreen(width, height);
        this.window = SDL_CreateWindow(spec.title(), windowSize[0], windowSize[1], SDL_WINDOW_RESIZABLE);
        if (window == MemoryUtil.NULL) {
            throw new PlatformException("Could not create the window: " + SDL_GetError());
        }
        SDL_SetWindowMinimumSize(window, Math.min(MIN_WINDOW_SIZE, width), Math.min(MIN_WINDOW_SIZE, height));
        SDL_SetWindowPosition(window, position(spec.left()), position(spec.top()));

        this.renderer = nSDL_CreateRenderer(window, MemoryUtil.NULL);
        if (renderer == MemoryUtil.NULL) {
            SDL_DestroyWindow(window);
            throw new PlatformException("Could not create the renderer: " + SDL_GetError());
        }
        applyPresentation();
        try {
            allocateTexture();
        } catch (PlatformException e) {
            SDL_DestroyRenderer(renderer);
            SDL_DestroyWindow(window);
            throw e;
        }
        this.id = SDL_GetWindowID(window);
        SDL_StartTextInput(window);
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public void present(FrameBuffer page, Size area, int borderArgb) {
        if (closed) {
            return;
        }
        if (area.width() != width || area.height() != height) {
            resizeTexture(area.width(), area.height());
        }
        updateScaleMode();
        page.copyTo(uploadPixels, width, height);
        SDL_UpdateTexture(texture, null, upload, width * Integer.BYTES);
        SDL_SetRenderDrawColor(renderer, (byte) (borderArgb >> 16), (byte) (borderArgb >> 8),
                (byte) borderArgb, (byte) 0xFF);
        SDL_RenderClear(renderer);
        SDL_RenderTexture(renderer, texture, null, null);
        SDL_RenderPresent(renderer);
    }

    @Override
    public Size outputSize() {
        if (!closed) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer outputWidth = stack.mallocInt(1);
                IntBuffer outputHeight = stack.mallocInt(1);
                if (SDL_GetRenderOutputSize(renderer, outputWidth, outputHeight)) {
                    return new Size(outputWidth.get(0), outputHeight.get(0));
                }
            }
        }
        return new Size(width, height);
    }

    @Override
    public void setScalingMode(ScalingMode mode) {
        scalingMode = mode;
        if (!closed) {
            applyPresentation();
        }
    }

    /** Applies the logical presentation according to the scaling mode and the page size. */
    private void applyPresentation() {
        int presentation = switch (scalingMode) {
            case RESIZE, KEEP_ASPECT -> SDL_LOGICAL_PRESENTATION_LETTERBOX;
            case STRETCH -> SDL_LOGICAL_PRESENTATION_STRETCH;
            case INTEGER -> SDL_LOGICAL_PRESENTATION_INTEGER_SCALE;
        };
        SDL_SetRenderLogicalPresentation(renderer, width, height, presentation);
    }

    /** Creates the texture and the upload buffer for the current page size. */
    private void allocateTexture() {
        texture = SDL_CreateTexture(renderer, SDL_PIXELFORMAT_ARGB8888, SDL_TEXTUREACCESS_STREAMING,
                width, height);
        if (texture == null) {
            throw new PlatformException("Could not create the texture: " + SDL_GetError());
        }
        upload = MemoryUtil.memAlloc(width * height * Integer.BYTES);
        uploadPixels = upload.asIntBuffer();
        scaleMode = -1;
    }

    /** Recreates the texture when the page changes size (RESIZE mode). */
    private void resizeTexture(int newWidth, int newHeight) {
        SDL_DestroyTexture(texture);
        MemoryUtil.memFree(upload);
        width = newWidth;
        height = newHeight;
        allocateTexture();
        applyPresentation();
    }

    @Override
    public void setTitle(String title) {
        if (!closed) {
            SDL_SetWindowTitle(window, title);
        }
    }

    /** Converts the mouse coordinates of an event to BGI coordinates. */
    public void toLogicalCoordinates(SDL_Event event) {
        if (!closed) {
            SDL_ConvertEventToRenderCoordinates(renderer, event);
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        SDL_DestroyTexture(texture);
        SDL_DestroyRenderer(renderer);
        SDL_DestroyWindow(window);
        MemoryUtil.memFree(upload);
        onClose.accept(this);
    }

    /**
     * Chooses the scaling filter: when shrinking, linear interpolation is used so
     * that one-pixel lines do not disappear; when enlarging, nearest neighbor is
     * used so that text and lines look sharp (smoothing
     * filters make small fonts look blurry).
     */
    private void updateScaleMode() {
        int wanted;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer outputWidth = stack.mallocInt(1);
            IntBuffer outputHeight = stack.mallocInt(1);
            if (!SDL_GetRenderOutputSize(renderer, outputWidth, outputHeight)) {
                return;
            }
            boolean shrinking = outputWidth.get(0) < width || outputHeight.get(0) < height;
            wanted = shrinking ? SDL_SCALEMODE_LINEAR : SDL_SCALEMODE_NEAREST;
        }
        if (wanted != scaleMode && SDL_SetTextureScaleMode(texture, wanted)) {
            scaleMode = wanted;
        }
    }

    /** Initial window size: the requested one, or shrunk if it does not fit on the screen. */
    private static int[] fitToScreen(int width, int height) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_Rect usable = SDL_Rect.malloc(stack);
            if (!SDL_GetDisplayUsableBounds(SDL_GetPrimaryDisplay(), usable)) {
                return new int[]{width, height};
            }
            double maxWidth = Math.max(MIN_WINDOW_SIZE, usable.w() - DECORATION_WIDTH);
            double maxHeight = Math.max(MIN_WINDOW_SIZE, usable.h() - DECORATION_HEIGHT);
            double factor = Math.min(1.0, Math.min(maxWidth / width, maxHeight / height));
            return new int[]{
                    Math.max(1, (int) Math.floor(width * factor)),
                    Math.max(1, (int) Math.floor(height * factor))};
        }
    }

    private static int position(int coordinate) {
        return coordinate == DisplaySpec.CENTERED ? SDL_WINDOWPOS_CENTERED_MASK : coordinate;
    }
}
