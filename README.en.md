# mvn-graphics

[Leer en español](README.md)

[![CI](https://github.com/programming0026/mvn-graphics/actions/workflows/ci.yml/badge.svg)](https://github.com/programming0026/mvn-graphics/actions/workflows/ci.yml)

A **Java 21** rewrite of the **BGI** graphics library (`graphics.h`) from Borland
Turbo C / WinBGI, using **SDL 3** through [LWJGL](https://www.lwjgl.org/).

It lets you port classic C graphics programs to Java almost line by line:

```c
// C / C++ with Borland BGI
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

## Repository contents

| Folder | What it is |
|--------|------------|
| [`mvn-sdl-graphics`](mvn-sdl-graphics/README.en.md) | The `com.juniverse.graphics:sdl-graphics` **library**: the complete BGI API on top of SDL 3. It has no main program; it is used as a dependency. |
| [`mvn-sdl-graphics-examples`](mvn-sdl-graphics-examples/README.en.md) | **Example programs**: a menu to pick them, a demonstration of the library and ported classic Turbo C programs (Borland's official demo `BGIDEMO.C`, fire effect, Fourier series, function grapher and 7-segment display). It builds an executable jar. |

They are two independent Maven projects: the examples use the library installed in the
local Maven repository.

## Requirements

| Tool | Version |
|------|---------|
| JDK | 21 or later |
| Maven | 3.9 or later |
| SDL 3 | No need to install it: LWJGL ships the native binaries for Windows, Linux and macOS |

## Quick start

```bash
# 1. Build, test and install the library
cd mvn-sdl-graphics
mvn clean install

# 2. Build the examples and open the menu
cd ../mvn-sdl-graphics-examples
mvn clean package
java -jar target/mvn-sdl-graphics-examples-1.0-0-all.jar
```

In the menu, pick an example with the keys `1` to `6`; `ESC` exits.

> **macOS:** SDL must run on the main thread. Use
> `java -XstartOnFirstThread -jar ...`.

## Using the library in your own project

After installing it with `mvn install`, add the dependency:

```xml
<dependency>
    <groupId>com.juniverse.graphics</groupId>
    <artifactId>sdl-graphics</artifactId>
    <version>1.0-0</version>
</dependency>
```

That dependency brings LWJGL, SDL 3 and the native binaries of the current operating
system. The library README contains the complete equivalence table with
`graphics.h`.

## Features

* **Complete BGI API:**
  - shapes, fills with Borland's 12 patterns and user patterns,
  - `floodFill`, `getPixel`/`putPixel`, XOR mode,
  - `getImage`/`putImage`,
  - text with 11 fonts and justification,
  - 4 drawing pages with double buffering,
  - 16-color palette and RGB colors,
  - `getch`/`kbhit`-style keyboard and mouse.
* **`initGraph(gd, gm, "")`** with Borland's drivers and modes (`DETECT`, `VGA`,
  `VGAHI`, `EGAHI`, `IBM8514HI`, ...) and `graphResult()`/`graphErrorMsg()`.
* **Resizable and maximizable window:** the drawing area can grow with the window
  or scale while keeping the aspect ratio.
* **8x8 bitmap font:** like BGI's, with all 256 characters of DOS code page 437,
  including accented letters, box-drawing lines, blocks and symbols.
  `CodePage437` converts DOS character codes when porting programs.
* **Image reading and writing:** BMP, PNG, JPG and GIF.
* **Windowless tests:** an in-memory fake platform (`FakePlatform`) allows testing
  complete programs in continuous integration.

## Architecture in brief

* **Drawing is done in pure Java** on in-memory pixel pages. SDL 3 only creates
  the window, shows the visible page and delivers keyboard and mouse events. This way
  `getPixel`, `floodFill`, XOR mode and `getImage` work exactly as in BGI.
* **Layered design with SOLID principles:**
  - a static `Bgi` facade with the `graphics.h` API,
  - windows and the event cycle,
  - drawing and text operations,
  - rasterization algorithms,
  - and a `Platform` port, whose only real implementation is the SDL one.
* **Conventions:**
  - records go in `dto` packages and exceptions in `exception` packages,
  - all classes and members declare their access modifier,
  - log and exception messages are in English,
  - code comments are in English; the READMEs are available in Spanish and English.

The details of the packages, the threading model and the differences with WinBGI are in the
[library README](mvn-sdl-graphics/README.en.md).

## Continuous integration

The [`.github/workflows/ci.yml`](.github/workflows/ci.yml) workflow runs on every push or
pull request to `main`, and can also be launched by hand from the *Actions* tab.

* It builds and tests the library (`mvn clean install`) and then the examples
  (`mvn clean verify`).
* It runs on **Windows, Linux and macOS**: this checks that each system downloads its
  own SDL 3 native binaries.
* The tests open no windows, so they work on GitHub's servers.
* It publishes as artifacts, one per operating system, the library jars (classes,
  sources, javadoc and tests) and the executable examples jar. If any test fails,
  it also publishes its reports.

## License

This project is distributed under the [MIT license](LICENSE).

## Credits

Based on the API of the *Borland Graphics Interface* and of **WinBGIm 6.0** (2004), by Grant
Macklem, Gregory Schmelter, Alan Schmidt, Ivan Stashak and Michael Main (University of
Colorado at Boulder).
