# SDL Graphics: WinBGI for Java 21 with SDL 3

[Leer en español](README.md)

A rewrite in **Java 21** of the **WinBGI** library (the Windows version of the
Borland Graphics Interface, `graphics.h`), using **SDL 3** through the
[LWJGL 3.4](https://www.lwjgl.org/) bindings.

The idea is that a program written with `graphics.h` can be migrated almost line by line:

```c
// C / C++ with Borland BGI / WinBGI
int gd = DETECT, gm = VGA;
initgraph(&gd, &gm, "");
setcolor(YELLOW);
circle(320, 240, 100);
setfillstyle(SOLID_FILL, RED);
floodfill(320, 240, YELLOW);
outtextxy(10, 10, "Hello");
getch();
closegraph();
```

```java
// Java with SDL Graphics
import static com.juniverse.graphics.api.Bgi.*;

int gd = DETECT, gm = VGA;
initGraph(gd, gm, "");
setColor(BgiColor.YELLOW);
circle(320, 240, 100);
setFillStyle(FillPattern.SOLID, BgiColor.RED);
floodFill(320, 240, BgiColor.YELLOW);
outTextXY(10, 10, "Hello");
getch();
closeGraph();
```

---

## Requirements

| Tool  | Version |
|-------|---------|
| JDK   | 21 or later |
| Maven | 3.9 or later |
| SDL 3 | No need to install it: LWJGL includes the native binaries (`SDL3.dll`, `libSDL3.so`, `libSDL3.dylib`) |

The `pom.xml` detects the operating system (Windows, Linux, macOS x64/arm64) and
automatically downloads the right natives.

## Build and install the library

This project is a **library**: it has no main program. To use it from other
projects, install it in the local Maven repository:

```bash
mvn clean install          # builds, runs the tests and installs into ~/.m2
mvn install -DskipTests    # installs without running the tests
```

`mvn package` generates in `target/`:

| File | Content |
|------|---------|
| `sdl-graphics-1.0-SNAPSHOT.jar` | The library classes |
| `sdl-graphics-1.0-SNAPSHOT-sources.jar` | The source code (so the IDE shows it when navigating) |
| `sdl-graphics-1.0-SNAPSHOT-javadoc.jar` | The Javadoc documentation |
| `sdl-graphics-1.0-SNAPSHOT-tests.jar` | The test classes, including `FakePlatform` (to test programs without opening windows) |

## Using the library in another project

Add the dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>com.juniverse.graphics</groupId>
    <artifactId>sdl-graphics</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

* That dependency automatically brings LWJGL, the SDL3 bindings and the
  **native binaries of the current operating system**. There is no need to declare LWJGL.
* To package for another system, say so when building your project:
  `-Dlwjgl.natives=natives-linux` (also `natives-windows`, `natives-macos` or
  `natives-macos-arm64`).
* To test your program without windows, also add the test classes:

  ```xml
  <dependency>
      <groupId>com.juniverse.graphics</groupId>
      <artifactId>sdl-graphics</artifactId>
      <version>1.0-SNAPSHOT</version>
      <type>test-jar</type>
      <scope>test</scope>
  </dependency>
  ```

  and configure the facade with `Bgi.configure(new WindowManager(() -> new FakePlatform(), ...))`.
* The jar declares the automatic module name `com.juniverse.graphics`.

> **macOS:** SDL has to run on the main thread. Programs that use the
> library must launch the JVM with `-XstartOnFirstThread`.

The example programs (and the executable fat jar) are in the sibling project
[`mvn-sdl-graphics-examples`](../mvn-sdl-graphics-examples/README.en.md).

The unit tests **open no windows**: they use an in-memory fake platform
(`FakePlatform`), so they can run on a continuous integration server.

---

## Architecture

### Main decision: rasterization in Java, SDL only for display

WinBGI drew with GDI on several in-memory *device contexts*. This version
does the same with its **own framebuffer** (an ARGB `int[]` per page) and uses SDL 3
only to:

1. create the window,
2. upload the visible page to a texture and present it,
3. read keyboard and mouse events.

This makes it possible to faithfully implement functions that would be complicated on a GPU:
`getpixel`, `floodfill`, the `XOR_PUT` mode, the 8x8 fill patterns,
`getimage`/`putimage` and the 4 drawing pages.

### Threading model

BGI is a **blocking** API (`getch`, `delay`). WinBGI used a separate thread per
window; here **everything happens on the program's thread**:

* Each drawing operation marks the window as modified and, at most about
  **60 times per second**, events are processed and the screen is refreshed.
* `delay`, `getch`, `kbhit`, `mouseX`, `isMouseClick`, etc. also process
  events, so the window never gets "frozen".

That is why all calls to the library must be made from a **single thread**.

### Packages

```
com.juniverse.graphics
├── api            Static Bgi facade (the "graphics.h" API)
├── driver         initGraph drivers and modes (DETECT, VGA, VGAHI, EGAHI, ...)
├── window         Windows, pages, window manager and event cycle
├── drawing        Graphics state and drawing operations (lines, shapes, fills)
├── raster         Framebuffer, canvas with viewport and rasterization algorithms
├── text           Fonts, justification and text rasterization
├── image          getImage/putImage and image files (BMP, PNG, JPG, GIF)
├── input          Keyboard (getch) and mouse
├── color          BGI colors, RGB codes and palette
├── style          Line styles, fill patterns and raster operations
├── dto            Records (immutable data): Point, Rect, Viewport, LineSettings,
│                  TextSettings, DisplaySpec, GraphicsMode, InputEvent, ...
├── exception      Exceptions: PlatformException, NoCurrentWindowException
└── platform       Port to the window system
    └── sdl        SDL 3 adapter (LWJGL): the only part that knows SDL
```

Code conventions:

* **Records** in the `dto` package and **exceptions** in the `exception` package.
* Each class and each member declares its access modifier (`public`, `private` or
  `protected`); the default package visibility is not used.
* Log and exception messages, the `graphErrorMsg` texts, window titles and the texts
  shown on screen are in **English**; code comments are also in English.

Dependencies between layers (arrows mean "depends on"):

```
api ──► window ──► drawing ──► raster ──► style / color      (all use dto and exception)
           │          ▲
           │          ├── text
           │          └── image
           ├──► input
           └──► platform (interfaces)  ◄── platform.sdl (implementation)
```

### SOLID principles applied

| Principle | How it is applied |
|-----------|-------------------|
| **S**: single responsibility | Each class does one thing: `LineRasterizer` only computes the pixels of a line, `PatternPainter` only paints spans with a pattern, `KeyboardBuffer` only manages the key queue, `SdlDisplay` only presents pixels. `GraphicsWindow` only composes the pieces. |
| **O**: open/closed | New backends (`Platform`), font engines (`GlyphRasterizer`) or image formats (`ImageFileStore`) can be added without modifying the core. The enums (`FillPattern`, `LineStyle`, `RasterOperation`) encapsulate their own behavior. |
| **L**: Liskov substitution | Any implementation of `Canvas`, `Platform`, `Display` or `GlyphRasterizer` is interchangeable; the tests replace SDL with `FakePlatform` without changing anything else. |
| **I**: interface segregation | Instead of one giant interface, the operations are split into `LineOperations`, `ShapeOperations`, `FillOperations`, `ScreenOperations`, `TextOperations` and `ImageOperations`. |
| **D**: dependency inversion | The core depends on abstractions (`Platform`, `Display`, `Canvas`, `GlyphRasterizer`, `ImageFileStore`, `FrameScheduler`, `SurfaceObserver`). Concrete implementations are injected from `Bgi` (composition root) or from the tests. |

### Two ways to use it

**1. Static facade (classic BGI style):**

```java
import static com.juniverse.graphics.api.Bgi.*;
```

**2. Object-oriented API** (useful for several windows or to inject dependencies):

```java
WindowManager manager = Bgi.windowManager();
int id = manager.openWindow(DisplaySpec.centered(800, 600, "My window"), WindowOptions.DEFAULT);
GraphicsWindow window = manager.current();
window.shapes().circle(400, 300, 100);
window.text().outTextXY(10, 10, "Hello");
manager.readKey();
manager.closeAll();
```

You can also create your own `WindowManager` with another `Platform` and
register it with `Bgi.configure(...)`.

---

## Equivalences with `graphics.h`

### Windows and control

| WinBGI | Java |
|--------|------|
| `int gd = DETECT, gm = VGA; initgraph(&gd, &gm, "")` | `int gd = DETECT, gm = VGA; initGraph(gd, gm, "")` (also `initGraph()`) |
| `initwindow(w, h, title, left, top, dbflag, closeflag)` | `initWindow(w, h, title, left, top, doubleBuffer, closeFlag)` (there are also shorter overloads; without a position, the window is centered) |
| `detectgraph(&gd, &gm)` | `detectGraph()` → `GraphicsMode` (always VGA / VGAHI) |
| `getgraphmode()`, `getmaxmode()`, `getmodename(m)` | `getGraphMode()`, `getMaxMode()`, `getModeName(m)` |
| `getmoderange(gd, &lo, &hi)` | `getModeRange(gd)` → `int[]{lo, hi}` |
| `setgraphmode(m)`, `restorecrtmode()` | `setGraphMode(m)` (clears and restores defaults; reuses the window if the resolution is the same or recreates it if it changes), `restoreCrtMode()` (does nothing: there is no text mode) |
| `getdrivername()` | `getDriverName()` (for example `"EGAVGA"`) |
| `isresizeevent()`, `clearresizeevent()` | `isResizeEvent()`, `clearResizeEvent()` |
| *(does not exist)* | `setWindowScaling(ScalingMode)`, `getWindowScaling()`: how the drawing adapts when maximized |
| `closegraph(wid)` | `closeGraph()`, `closeGraph(id)`, `closeGraph(CURRENT_WINDOW)` |
| `getcurrentwindow()` / `setcurrentwindow(id)` | `getCurrentWindow()` / `setCurrentWindow(id)` |
| `graphdefaults()`, `graphresult()`, `grapherrormsg(e)` | `graphDefaults()`, `graphResult()`, `graphErrorMsg(e)` |
| `getmaxx()`, `getmaxy()`, `getwindowwidth()`, `getwindowheight()` | `getMaxX()`, `getMaxY()`, `getWindowWidth()`, `getWindowHeight()` |
| `getmaxwidth()`, `getmaxheight()` | `getMaxWidth()`, `getMaxHeight()` (screen size) |
| `delay(ms)` | `delay(ms)` |
| `showerrorbox(msg)` | `showErrorBox(msg)` |

### Drawing

| WinBGI | Java |
|--------|------|
| `putpixel`, `getpixel` | `putPixel`, `getPixel` |
| `line`, `lineto`, `linerel`, `moveto`, `moverel`, `getx`, `gety` | `line`, `lineTo`, `lineRel`, `moveTo`, `moveRel`, `getX`, `getY` |
| `rectangle`, `circle`, `arc`, `ellipse` | `rectangle`, `circle`, `arc`, `ellipse` |
| `drawpoly(n, pts)`, `fillpoly(n, pts)` | `drawPoly(x1, y1, x2, y2, ...)`, `fillPoly(...)` (or with `List<Point>`) |
| `bar`, `bar3d`, `fillellipse`, `pieslice`, `sector`, `floodfill` | `bar`, `bar3d`, `fillEllipse`, `pieSlice`, `sector`, `floodFill` |
| `cleardevice`, `clearviewport` | `clearDevice`, `clearViewport` |
| `getarccoords(&ac)` | `getArcCoords()` → `ArcCoordinates` |

### Styles and colors

| WinBGI | Java |
|--------|------|
| `setcolor`, `getcolor`, `setbkcolor`, `getbkcolor` | `setColor`, `getColor`, `setBkColor`, `getBkColor` (accept `int` or `BgiColor`) |
| `COLOR(r, g, b)` | `rgb(r, g, b)` |
| `RED_VALUE(c)`, `GREEN_VALUE(c)`, `BLUE_VALUE(c)`, `converttorgb(c)` | `redValue(c)`, `greenValue(c)`, `blueValue(c)`, `convertToRgb(c)` |
| `setlinestyle(style, pattern, thickness)` | `setLineStyle(LineStyle, pattern, thickness)` |
| `setfillstyle(pattern, color)`, `setfillpattern(p, color)` | `setFillStyle(FillPattern, color)`, `setFillPattern(int[8], color)` |
| `setwritemode(COPY_PUT / XOR_PUT)` | `setWriteMode(RasterOperation.COPY / XOR)` |
| `setviewport(l, t, r, b, clip)`, `getviewsettings` | `setViewport(l, t, r, b, clip)`, `getViewSettings()` |
| `setpalette`, `setrgbpalette`, `getpalette`, `setallpalette` | `setPalette`, `setRgbPalette`, `getPalette`, `setAllPalette` |

### Text

| WinBGI | Java |
|--------|------|
| `outtext`, `outtextxy` | `outText`, `outTextXY` |
| `settextstyle(font, dir, size)` | `setTextStyle(FontName, TextDirection, size)` |
| `settextjustify(h, v)` | `setTextJustify(HorizontalAlignment, VerticalAlignment)` |
| `setusercharsize`, `textwidth`, `textheight`, `gettextsettings` | `setUserCharSize`, `textWidth`, `textHeight`, `getTextSettings` |
| `bgiout << ...; outstream()` | Use `String.format(...)` or `StringBuilder` with `outText(...)` |

### Images and pages

| WinBGI | Java |
|--------|------|
| `imagesize`, `getimage`, `putimage(x, y, bmp, op)` | `imageSize`, `getImage` → `BgiImage`, `putImage(x, y, image, RasterOperation)` |
| `readimagefile(file, l, t, r, b)` | `readImageFile(file, l, t, r, b)` (BMP, PNG, JPG, GIF) |
| `writeimagefile(file, l, t, r, b)` | `writeImageFile(file, l, t, r, b)` (format according to the extension) |
| `setactivepage`, `setvisualpage`, `swapbuffers` | `setActivePage`, `setVisualPage`, `swapBuffers` |
| `getrefreshingbgi`, `setrefreshingbgi`, `refreshallbgi` | `getRefreshingBgi`, `setRefreshingBgi`, `refreshAllBgi` |

### Keyboard and mouse

| WinBGI | Java |
|--------|------|
| `getch()`, `kbhit()` | `getch()`, `kbhit()`; extended keys return `0` and then the code (`ExtendedKey.UP.code()`, ...) |
| `mousex()`, `mousey()` | `mouseX()`, `mouseY()` |
| `ismouseclick(WM_LBUTTONDOWN)` | `isMouseClick(MouseEventKind.LEFT_BUTTON_DOWN)` |
| `getmouseclick(kind, x, y)` | `getMouseClick(kind)` → `Point` (or `Point.NONE` if there was no click) |
| `clearmouseclick`, `setmousequeuestatus` | `clearMouseClick`, `setMouseQueueStatus` |
| `registermousehandler(kind, handler)` | `registerMouseHandler(kind, (x, y) -> { ... })` |

### Constants

The C constants became enums with descriptive names:

| WinBGI | Java |
|--------|------|
| `BLACK ... WHITE`, `LIGHTGRAY` | `BgiColor.BLACK ... BgiColor.WHITE`, `BgiColor.LIGHT_GRAY` |
| `SOLID_LINE`, `DOTTED_LINE`, `CENTER_LINE`, `DASHED_LINE`, `USERBIT_LINE` | `LineStyle.SOLID`, `DOTTED`, `CENTER`, `DASHED`, `USER_BIT` |
| `EMPTY_FILL ... CLOSE_DOT_FILL`, `USER_FILL` | `FillPattern.EMPTY ... CLOSE_DOT`, `USER` |
| `COPY_PUT`, `XOR_PUT`, `OR_PUT`, `AND_PUT`, `NOT_PUT` | `RasterOperation.COPY`, `XOR`, `OR`, `AND`, `NOT` |
| `DEFAULT_FONT ... BOLD_FONT` | `FontName.DEFAULT ... BOLD` |
| `HORIZ_DIR`, `VERT_DIR` | `TextDirection.HORIZONTAL`, `VERTICAL` |
| `LEFT_TEXT`, `CENTER_TEXT`, `RIGHT_TEXT` | `HorizontalAlignment.LEFT`, `CENTER`, `RIGHT` |
| `BOTTOM_TEXT`, `VCENTER_TEXT`, `TOP_TEXT` | `VerticalAlignment.BOTTOM`, `CENTER`, `TOP` |
| `KEY_UP`, `KEY_F1`, ... | `ExtendedKey.UP`, `ExtendedKey.F1`, ... |
| `NORM_WIDTH`, `THICK_WIDTH`, `USER_CHAR_SIZE` | `Bgi.NORM_WIDTH`, `Bgi.THICK_WIDTH`, `Bgi.USER_CHAR_SIZE` |
| `DETECT`, `CGA`, `EGA`, `VGA`, `IBM8514`, ... | Same: `Bgi.DETECT`, `Bgi.VGA`, ... (`GraphicsDriver` enum for the object-oriented API) |
| `VGALO`, `VGAMED`, `VGAHI`, `EGAHI`, `CGAHI`, ... | Same: `Bgi.VGAHI`, `Bgi.EGAHI`, ... |
| `grOk` | `Bgi.GR_OK` |

### `initGraph` graphics modes

| Driver | Modes (resolution) |
|--------|--------------------|
| `DETECT` | Ignores the mode and chooses `VGA` / `VGAHI` (640x480) |
| `VGA` | `VGALO` 640x200, `VGAMED` 640x350, `VGAHI` 640x480 |
| `EGA`, `EGA64` | `EGALO` 640x200, `EGAHI` 640x350 |
| `EGAMONO` | `EGAMONOHI` 640x350 |
| `CGA` | `CGAC0`–`CGAC3` 320x200, `CGAHI` 640x200 |
| `MCGA` | `MCGAC0`–`MCGAC3` 320x200, `MCGAMED` 640x200, `MCGAHI` 640x480 |
| `ATT400` | `ATT400C0`–`C3` 320x200, `ATT400MED` 640x200, `ATT400HI` 640x400 |
| `HERCMONO` | `HERCMONOHI` 720x348 |
| `PC3270` | `PC3270HI` 720x350 |
| `IBM8514` | `IBM8514LO` 640x480, `IBM8514HI` 1024x768 |

An invalid driver or mode does not open the window and leaves the error in `graphResult()`,
just as in Borland:

```java
int gd = DETECT, gm = VGA;
initGraph(gd, gm, "");
int errorCode = graphResult();
if (errorCode != GR_OK) {
    System.err.println("Graphics error: " + graphErrorMsg(errorCode));
    System.exit(1);
}
```

## Resizable window and scaling

The window can be **maximized, resized or dragged**. What happens to the drawing
depends on the mode chosen with `setWindowScaling(...)`:

| Mode | When maximized |
|------|----------------|
| `ScalingMode.RESIZE` (**default**) | The **drawing area grows** with the window: `getMaxX()`/`getMaxY()` become the real size of the window and nothing is scaled (sharp text; a bouncing ball reaches the real edges). The visible area never drops below the initial resolution. What was drawn is **never lost**: if the window shrinks or is minimized, only the visible part shrinks, and what was left outside is visible again when maximizing it once more. |
| `ScalingMode.KEEP_ASPECT` | The BGI resolution does not change; the image is enlarged **keeping the aspect ratio** and the leftover space is filled with the background color (`setBkColor`). |
| `ScalingMode.STRETCH` | The BGI resolution does not change; the image is **stretched** to cover the whole window, even if it gets distorted. |
| `ScalingMode.INTEGER` | The BGI resolution does not change; it is enlarged only by integer factors (x2, x3, ...). It is the **sharpest** mode when scaling. |

* In `RESIZE` mode, a program that wants to take advantage of the new size must use
  `getMaxX()`/`getMaxY()` and redraw when `isResizeEvent()` is true (the
  `DemoGraphics` demonstration does so with `showUntilKey`). Programs with fixed coordinates
  keep working: their drawing stays in the top-left corner.
* When there is scaling, enlarging uses the nearest pixel (sharp text and lines) and
  shrinking uses linear interpolation (so one-pixel lines do not disappear).
* If the requested resolution does not fit on the screen (for example, `initWindow(3000, 2000)` on
  a 1920x1080 monitor), the window opens shrunk so that it is fully visible.
* Mouse coordinates are always converted to BGI coordinates. A click on the black
  bands is adjusted to the nearest edge of the drawing area.
* `isResizeEvent()` tells whether the user changed the window size.

> For a program to look good in **any** graphics mode, compute the coordinates
> from `getMaxX()` and `getMaxY()` instead of using fixed numbers (the
> `DemoGraphics` demonstration does so with the `sx()` and `sy()` functions).

---

## Differences from WinBGI

* **RGB color codes:** they are encoded as `0x03RRGGBB` (WinBGI used Windows'
  BGR order, `0x03BBGGRR`). If you use `rgb(r, g, b)` you will not notice the difference.
* **Fonts:** the default font (`FontName.DEFAULT`) is an 8x8 bitmap font
  like BGI's: each character measures `8 * charSize` pixels. It includes all 256
  characters of DOS code page 437: ASCII, accented letters (á é ñ ü ç ...),
  symbols (☺ ♥ ♪ ► ↑ ...), box-drawing lines (│ ┤ ╔ ═ ╬ ...), blocks (░ ▒ ▓ █),
  Greek letters and mathematical signs (± ≥ ≤ ÷ √ ∞ ...). Borland's vector fonts (`.CHR`: TRIPLEX, SANS_SERIF, GOTHIC, ...) are
  replaced by Java's logical fonts (Serif, SansSerif), rasterized without
  anti-aliasing; for them `textWidth`/`textHeight` return the real measurements of those fonts.
  Characters that a font cannot draw (for example, control characters) are shown
  as `?` instead of an empty box.
* **DOS characters:** in Turbo C a `char` was a byte of code page 437, but
  in Java a `char` is Unicode. When porting programs that write numeric codes (for
  example `buffer[0] = 130`), convert them with `CodePage437.toUnicode(130)` (returns `é`)
  or `CodePage437.decode(bytes)`. If you just cast the number to `char`, codes 128..159
  become invisible control characters and the others do not match those of DOS.
* **`getImage` / `putImage`:** instead of a `void*` memory block, the immutable
  `BgiImage` object is used.
* **`refreshBgi(l, t, r, b)`:** refreshes the whole window (presenting a whole
  texture is just as fast as presenting a region).
* **`printimage`, `installuserdriver`, `registerbgidriver`, `setgraphbufsize`:**
  were not implemented because WinBGI did not support them either or they depend on Windows.
* **I/O errors** in `readImageFile`/`writeImageFile` throw `UncheckedIOException`
  (and also leave `graphResult()` at `IO_ERROR`).
* **Threads:** all calls must be made from the same thread (see
  [Threading model](#threading-model)).

## Examples

The examples, including the ported classic Turbo C programs (`BGIDEMO.C`,
`DISPLAY.C`, `FOURIER.C`, `FIRE.C`, `GRAFICA.C`), are in the sibling project
[`mvn-sdl-graphics-examples`](../mvn-sdl-graphics-examples/README.en.md), which uses this
library as a dependency.

## Tests

```bash
mvn test
```

| Test class | What it verifies |
|------------|------------------|
| `RasterizerTest` | Bresenham, polygon and ellipse fill, flood fill, viewport and clipping |
| `PaletteTest` | BGI/RGB color conversion, palette and XOR operation |
| `InputTest` | Extended keys, click queue and mouse handlers |
| `BitmapFontTest` | 8x8 font: cell size, glyph pixels, scaling, Spanish characters and engine selection by font |
| `ModeResolverTest` | `DETECT`, resolution of each mode, driver/mode errors and ranges |
| `GraphicsWindowTest` | Integration of a complete window on `FakePlatform`: drawing, XOR, viewport, text, images, pages, events, refresh, resizing and `setGraphMode` |

## Credits

Based on the API of **WinBGIm 6.0** (2004), by Grant Macklem, Gregory Schmelter,
Alan Schmidt, Ivan Stashak and Michael Main (University of Colorado at Boulder),
which in turn reproduces the *Borland Graphics Interface*.
