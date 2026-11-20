# SDL Graphics: WinBGI para Java 21 con SDL 3

[Read in English](README.en.md)

Reescritura en **Java 21** de la biblioteca **WinBGI** (la versión para Windows de
Borland Graphics Interface, `graphics.h`), usando **SDL 3** a través de los bindings de
[LWJGL 3.4](https://www.lwjgl.org/).

La idea es que un programa escrito con `graphics.h` se pueda migrar casi línea por línea:

```c
// C / C++ con Borland BGI / WinBGI
int gd = DETECT, gm = VGA;
initgraph(&gd, &gm, "");
setcolor(YELLOW);
circle(320, 240, 100);
setfillstyle(SOLID_FILL, RED);
floodfill(320, 240, YELLOW);
outtextxy(10, 10, "Hola");
getch();
closegraph();
```

```java
// Java con SDL Graphics
import static com.juniverse.graphics.api.Bgi.*;

int gd = DETECT, gm = VGA;
initGraph(gd, gm, "");
setColor(BgiColor.YELLOW);
circle(320, 240, 100);
setFillStyle(FillPattern.SOLID, BgiColor.RED);
floodFill(320, 240, BgiColor.YELLOW);
outTextXY(10, 10, "Hola");
getch();
closeGraph();
```

---

## Requisitos

| Herramienta | Versión |
|-------------|---------|
| JDK         | 21 o superior |
| Maven       | 3.9 o superior |
| SDL 3       | No hay que instalarlo: LWJGL incluye los binarios nativos (`SDL3.dll`, `libSDL3.so`, `libSDL3.dylib`) |

El `pom.xml` detecta el sistema operativo (Windows, Linux, macOS x64/arm64) y descarga
automáticamente los nativos correctos.

## Compilar e instalar la biblioteca

Este proyecto es una **biblioteca**: no tiene programa principal. Para usarla desde otros
proyectos, instálela en el repositorio local de Maven:

```bash
mvn clean install          # compila, ejecuta las pruebas e instala en ~/.m2
mvn install -DskipTests    # instala sin ejecutar las pruebas
```

`mvn package` genera en `target/`:

| Archivo | Contenido |
|---------|-----------|
| `sdl-graphics-1.0-SNAPSHOT.jar` | Las clases de la biblioteca |
| `sdl-graphics-1.0-SNAPSHOT-sources.jar` | El código fuente (para que el IDE lo muestre al navegar) |
| `sdl-graphics-1.0-SNAPSHOT-javadoc.jar` | La documentación Javadoc |
| `sdl-graphics-1.0-SNAPSHOT-tests.jar` | Las clases de prueba, incluida `FakePlatform` (para probar programas sin abrir ventanas) |

## Usar la biblioteca en otro proyecto

Agregue la dependencia en su `pom.xml`:

```xml
<dependency>
    <groupId>com.juniverse.graphics</groupId>
    <artifactId>sdl-graphics</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

* Con esa dependencia llegan automáticamente LWJGL, los bindings de SDL3 y los
  **binarios nativos del sistema operativo actual**. No hace falta declarar LWJGL.
* Para empaquetar para otro sistema, indíquelo al compilar su proyecto:
  `-Dlwjgl.natives=natives-linux` (también `natives-windows`, `natives-macos` o
  `natives-macos-arm64`).
* Para probar su programa sin ventanas, agregue también las clases de prueba:

  ```xml
  <dependency>
      <groupId>com.juniverse.graphics</groupId>
      <artifactId>sdl-graphics</artifactId>
      <version>1.0-SNAPSHOT</version>
      <type>test-jar</type>
      <scope>test</scope>
  </dependency>
  ```

  y configure la fachada con `Bgi.configure(new WindowManager(() -> new FakePlatform(), ...))`.
* El jar declara el nombre de módulo automático `com.juniverse.graphics`.

> **macOS:** SDL tiene que ejecutarse en el hilo principal. Los programas que usen la
> biblioteca deben lanzar la JVM con `-XstartOnFirstThread`.

Los programas de ejemplo (y el fat jar ejecutable) están en el proyecto hermano
[`mvn-sdl-graphics-examples`](../mvn-sdl-graphics-examples/README.md).

Las pruebas unitarias **no abren ventanas**: usan una plataforma falsa en memoria
(`FakePlatform`), así que se pueden ejecutar en un servidor de integración continua.

---

## Arquitectura

### Decisión principal: rasterizado en Java, SDL solo para mostrar

WinBGI dibujaba con GDI sobre varios *device contexts* en memoria. Esta versión
hace lo mismo con un **framebuffer propio** (`int[]` ARGB por página) y usa SDL 3
únicamente para:

1. crear la ventana,
2. subir la página visible a una textura y presentarla,
3. leer los eventos de teclado y mouse.

Así se pueden implementar fielmente funciones que en una GPU serían complicadas:
`getpixel`, `floodfill`, el modo `XOR_PUT`, los patrones de relleno de 8x8,
`getimage`/`putimage` y las 4 páginas de dibujo.

### Modelo de hilos

BGI es una API **bloqueante** (`getch`, `delay`). WinBGI usaba un hilo aparte por
ventana; aquí **todo ocurre en el hilo del programa**:

* Cada operación de dibujo marca la ventana como modificada y, como máximo unas
  **60 veces por segundo**, se procesan los eventos y se refresca la pantalla.
* `delay`, `getch`, `kbhit`, `mouseX`, `isMouseClick`, etc. también procesan
  los eventos, por lo que la ventana nunca se queda "congelada".

Por eso todas las llamadas a la biblioteca deben hacerse desde **un único hilo**.

### Paquetes

```
com.juniverse.graphics
├── api            Fachada estática Bgi (la API "graphics.h")
├── driver         Controladores y modos de initGraph (DETECT, VGA, VGAHI, EGAHI, ...)
├── window         Ventanas, páginas, gestor de ventanas y ciclo de eventos
├── drawing        Estado gráfico y operaciones de dibujo (líneas, figuras, rellenos)
├── raster         Framebuffer, lienzo con viewport y algoritmos de rasterizado
├── text           Fuentes, justificación y rasterizado de texto
├── image          getImage/putImage y archivos de imagen (BMP, PNG, JPG, GIF)
├── input          Teclado (getch) y mouse
├── color          Colores BGI, códigos RGB y paleta
├── style          Estilos de línea, patrones de relleno y operaciones raster
├── dto            Records (datos inmutables): Point, Rect, Viewport, LineSettings,
│                  TextSettings, DisplaySpec, GraphicsMode, InputEvent, ...
├── exception      Excepciones: PlatformException, NoCurrentWindowException
└── platform       Puerto hacia el sistema de ventanas
    └── sdl        Adaptador SDL 3 (LWJGL): la única parte que conoce SDL
```

Convenciones de código:

* **Records** en el paquete `dto` y **excepciones** en el paquete `exception`.
* Cada clase y cada miembro declara su modificador de acceso (`public`, `private` o
  `protected`); no se usa la visibilidad por defecto del paquete.
* Los mensajes de log y de excepción, los textos de `graphErrorMsg`, los títulos de
  ventana y los textos que se muestran en pantalla están en **inglés**; los comentarios y
  la documentación, en español.

Dependencias entre capas (las flechas indican "depende de"):

```
api ──► window ──► drawing ──► raster ──► style / color      (todas usan dto y exception)
           │          ▲
           │          ├── text
           │          └── image
           ├──► input
           └──► platform (interfaces)  ◄── platform.sdl (implementación)
```

### Principios SOLID aplicados

| Principio | Cómo se aplica |
|-----------|----------------|
| **S**: responsabilidad única | Cada clase hace una sola cosa: `LineRasterizer` solo calcula píxeles de una recta, `PatternPainter` solo pinta tramos con un patrón, `KeyboardBuffer` solo gestiona la cola de teclas, `SdlDisplay` solo presenta píxeles. `GraphicsWindow` únicamente compone las piezas. |
| **O**: abierto/cerrado | Se pueden añadir nuevos backends (`Platform`), motores de fuentes (`GlyphRasterizer`) o formatos de imagen (`ImageFileStore`) sin modificar el núcleo. Los enums (`FillPattern`, `LineStyle`, `RasterOperation`) encapsulan su propio comportamiento. |
| **L**: sustitución de Liskov | Cualquier implementación de `Canvas`, `Platform`, `Display` o `GlyphRasterizer` es intercambiable; las pruebas sustituyen SDL por `FakePlatform` sin cambiar nada más. |
| **I**: segregación de interfaces | En lugar de una interfaz gigante, las operaciones se dividen en `LineOperations`, `ShapeOperations`, `FillOperations`, `ScreenOperations`, `TextOperations` e `ImageOperations`. |
| **D**: inversión de dependencias | El núcleo depende de abstracciones (`Platform`, `Display`, `Canvas`, `GlyphRasterizer`, `ImageFileStore`, `FrameScheduler`, `SurfaceObserver`). Las implementaciones concretas se inyectan desde `Bgi` (raíz de composición) o desde las pruebas. |

### Dos formas de uso

**1. Fachada estática (estilo BGI clásico):**

```java
import static com.juniverse.graphics.api.Bgi.*;
```

**2. API orientada a objetos** (útil para varias ventanas o para inyectar dependencias):

```java
WindowManager manager = Bgi.windowManager();
int id = manager.openWindow(DisplaySpec.centered(800, 600, "Mi ventana"), WindowOptions.DEFAULT);
GraphicsWindow window = manager.current();
window.shapes().circle(400, 300, 100);
window.text().outTextXY(10, 10, "Hola");
manager.readKey();
manager.closeAll();
```

También se puede crear un `WindowManager` propio con otra `Platform` y
registrarlo con `Bgi.configure(...)`.

---

## Equivalencias con `graphics.h`

### Ventanas y control

| WinBGI | Java |
|--------|------|
| `int gd = DETECT, gm = VGA; initgraph(&gd, &gm, "")` | `int gd = DETECT, gm = VGA; initGraph(gd, gm, "")` (también `initGraph()`) |
| `initwindow(w, h, title, left, top, dbflag, closeflag)` | `initWindow(w, h, title, left, top, doubleBuffer, closeFlag)` (también hay sobrecargas más cortas; sin posición, la ventana sale centrada) |
| `detectgraph(&gd, &gm)` | `detectGraph()` → `GraphicsMode` (siempre VGA / VGAHI) |
| `getgraphmode()`, `getmaxmode()`, `getmodename(m)` | `getGraphMode()`, `getMaxMode()`, `getModeName(m)` |
| `getmoderange(gd, &lo, &hi)` | `getModeRange(gd)` → `int[]{lo, hi}` |
| `setgraphmode(m)`, `restorecrtmode()` | `setGraphMode(m)` (borra y restaura valores por defecto; reutiliza la ventana si la resolución es la misma o la recrea si cambia), `restoreCrtMode()` (no hace nada: no hay modo texto) |
| `getdrivername()` | `getDriverName()` (por ejemplo `"EGAVGA"`) |
| `isresizeevent()`, `clearresizeevent()` | `isResizeEvent()`, `clearResizeEvent()` |
| *(no existe)* | `setWindowScaling(ScalingMode)`, `getWindowScaling()`: cómo se ajusta el dibujo al maximizar |
| `closegraph(wid)` | `closeGraph()`, `closeGraph(id)`, `closeGraph(CURRENT_WINDOW)` |
| `getcurrentwindow()` / `setcurrentwindow(id)` | `getCurrentWindow()` / `setCurrentWindow(id)` |
| `graphdefaults()`, `graphresult()`, `grapherrormsg(e)` | `graphDefaults()`, `graphResult()`, `graphErrorMsg(e)` |
| `getmaxx()`, `getmaxy()`, `getwindowwidth()`, `getwindowheight()` | `getMaxX()`, `getMaxY()`, `getWindowWidth()`, `getWindowHeight()` |
| `getmaxwidth()`, `getmaxheight()` | `getMaxWidth()`, `getMaxHeight()` (tamaño de la pantalla) |
| `delay(ms)` | `delay(ms)` |
| `showerrorbox(msg)` | `showErrorBox(msg)` |

### Dibujo

| WinBGI | Java |
|--------|------|
| `putpixel`, `getpixel` | `putPixel`, `getPixel` |
| `line`, `lineto`, `linerel`, `moveto`, `moverel`, `getx`, `gety` | `line`, `lineTo`, `lineRel`, `moveTo`, `moveRel`, `getX`, `getY` |
| `rectangle`, `circle`, `arc`, `ellipse` | `rectangle`, `circle`, `arc`, `ellipse` |
| `drawpoly(n, pts)`, `fillpoly(n, pts)` | `drawPoly(x1, y1, x2, y2, ...)`, `fillPoly(...)` (o con `List<Point>`) |
| `bar`, `bar3d`, `fillellipse`, `pieslice`, `sector`, `floodfill` | `bar`, `bar3d`, `fillEllipse`, `pieSlice`, `sector`, `floodFill` |
| `cleardevice`, `clearviewport` | `clearDevice`, `clearViewport` |
| `getarccoords(&ac)` | `getArcCoords()` → `ArcCoordinates` |

### Estilos y colores

| WinBGI | Java |
|--------|------|
| `setcolor`, `getcolor`, `setbkcolor`, `getbkcolor` | `setColor`, `getColor`, `setBkColor`, `getBkColor` (aceptan `int` o `BgiColor`) |
| `COLOR(r, g, b)` | `rgb(r, g, b)` |
| `RED_VALUE(c)`, `GREEN_VALUE(c)`, `BLUE_VALUE(c)`, `converttorgb(c)` | `redValue(c)`, `greenValue(c)`, `blueValue(c)`, `convertToRgb(c)` |
| `setlinestyle(style, pattern, thickness)` | `setLineStyle(LineStyle, pattern, thickness)` |
| `setfillstyle(pattern, color)`, `setfillpattern(p, color)` | `setFillStyle(FillPattern, color)`, `setFillPattern(int[8], color)` |
| `setwritemode(COPY_PUT / XOR_PUT)` | `setWriteMode(RasterOperation.COPY / XOR)` |
| `setviewport(l, t, r, b, clip)`, `getviewsettings` | `setViewport(l, t, r, b, clip)`, `getViewSettings()` |
| `setpalette`, `setrgbpalette`, `getpalette`, `setallpalette` | `setPalette`, `setRgbPalette`, `getPalette`, `setAllPalette` |

### Texto

| WinBGI | Java |
|--------|------|
| `outtext`, `outtextxy` | `outText`, `outTextXY` |
| `settextstyle(font, dir, size)` | `setTextStyle(FontName, TextDirection, size)` |
| `settextjustify(h, v)` | `setTextJustify(HorizontalAlignment, VerticalAlignment)` |
| `setusercharsize`, `textwidth`, `textheight`, `gettextsettings` | `setUserCharSize`, `textWidth`, `textHeight`, `getTextSettings` |
| `bgiout << ...; outstream()` | Use `String.format(...)` o `StringBuilder` con `outText(...)` |

### Imágenes y páginas

| WinBGI | Java |
|--------|------|
| `imagesize`, `getimage`, `putimage(x, y, bmp, op)` | `imageSize`, `getImage` → `BgiImage`, `putImage(x, y, image, RasterOperation)` |
| `readimagefile(file, l, t, r, b)` | `readImageFile(file, l, t, r, b)` (BMP, PNG, JPG, GIF) |
| `writeimagefile(file, l, t, r, b)` | `writeImageFile(file, l, t, r, b)` (formato según la extensión) |
| `setactivepage`, `setvisualpage`, `swapbuffers` | `setActivePage`, `setVisualPage`, `swapBuffers` |
| `getrefreshingbgi`, `setrefreshingbgi`, `refreshallbgi` | `getRefreshingBgi`, `setRefreshingBgi`, `refreshAllBgi` |

### Teclado y mouse

| WinBGI | Java |
|--------|------|
| `getch()`, `kbhit()` | `getch()`, `kbhit()`; las teclas extendidas devuelven `0` y luego el código (`ExtendedKey.UP.code()`, ...) |
| `mousex()`, `mousey()` | `mouseX()`, `mouseY()` |
| `ismouseclick(WM_LBUTTONDOWN)` | `isMouseClick(MouseEventKind.LEFT_BUTTON_DOWN)` |
| `getmouseclick(kind, x, y)` | `getMouseClick(kind)` → `Point` (o `Point.NONE` si no hubo clic) |
| `clearmouseclick`, `setmousequeuestatus` | `clearMouseClick`, `setMouseQueueStatus` |
| `registermousehandler(kind, handler)` | `registerMouseHandler(kind, (x, y) -> { ... })` |

### Constantes

Las constantes de C se convirtieron en enums con nombres descriptivos:

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
| `DETECT`, `CGA`, `EGA`, `VGA`, `IBM8514`, ... | Iguales: `Bgi.DETECT`, `Bgi.VGA`, ... (enum `GraphicsDriver` para la API orientada a objetos) |
| `VGALO`, `VGAMED`, `VGAHI`, `EGAHI`, `CGAHI`, ... | Iguales: `Bgi.VGAHI`, `Bgi.EGAHI`, ... |
| `grOk` | `Bgi.GR_OK` |

### Modos gráficos de `initGraph`

| Controlador | Modos (resolución) |
|-------------|--------------------|
| `DETECT` | Ignora el modo y elige `VGA` / `VGAHI` (640x480) |
| `VGA` | `VGALO` 640x200, `VGAMED` 640x350, `VGAHI` 640x480 |
| `EGA`, `EGA64` | `EGALO` 640x200, `EGAHI` 640x350 |
| `EGAMONO` | `EGAMONOHI` 640x350 |
| `CGA` | `CGAC0`–`CGAC3` 320x200, `CGAHI` 640x200 |
| `MCGA` | `MCGAC0`–`MCGAC3` 320x200, `MCGAMED` 640x200, `MCGAHI` 640x480 |
| `ATT400` | `ATT400C0`–`C3` 320x200, `ATT400MED` 640x200, `ATT400HI` 640x400 |
| `HERCMONO` | `HERCMONOHI` 720x348 |
| `PC3270` | `PC3270HI` 720x350 |
| `IBM8514` | `IBM8514LO` 640x480, `IBM8514HI` 1024x768 |

Un controlador o modo inválido no abre la ventana y deja el error en `graphResult()`,
igual que en Borland:

```java
int gd = DETECT, gm = VGA;
initGraph(gd, gm, "");
int errorCode = graphResult();
if (errorCode != GR_OK) {
    System.err.println("Error gráfico: " + graphErrorMsg(errorCode));
    System.exit(1);
}
```

## Ventana redimensionable y escalado

La ventana se puede **maximizar, redimensionar o arrastrar**. Lo que ocurre con el dibujo
depende del modo elegido con `setWindowScaling(...)`:

| Modo | Al maximizar |
|------|--------------|
| `ScalingMode.RESIZE` (**por defecto**) | El **área de dibujo crece** con la ventana: `getMaxX()`/`getMaxY()` pasan a ser el tamaño real de la ventana y no se escala nada (texto nítido; una pelota que rebota llega a los bordes reales). El área visible nunca baja de la resolución inicial. Lo dibujado **nunca se pierde**: si la ventana se reduce o se minimiza, solo se achica la parte visible, y lo que quedó fuera vuelve a verse al maximizarla otra vez. |
| `ScalingMode.KEEP_ASPECT` | La resolución BGI no cambia; la imagen se amplía **conservando la proporción** y el espacio sobrante se rellena con el color de fondo (`setBkColor`). |
| `ScalingMode.STRETCH` | La resolución BGI no cambia; la imagen se **estira** hasta cubrir toda la ventana, aunque se deforme. |
| `ScalingMode.INTEGER` | La resolución BGI no cambia; se amplía solo por factores enteros (x2, x3, ...). Es el modo **más nítido** cuando se escala. |

* En modo `RESIZE`, un programa que quiera aprovechar el nuevo tamaño debe usar
  `getMaxX()`/`getMaxY()` y volver a dibujar cuando `isResizeEvent()` sea verdadero (la
  demostración `DemoGraphics` lo hace con `showUntilKey`). Los programas con coordenadas fijas
  siguen funcionando: su dibujo queda en la esquina superior izquierda.
* Cuando hay escalado, al ampliar se usa el píxel más cercano (texto y líneas nítidos) y
  al reducir interpolación lineal (para que no desaparezcan las líneas de un píxel).
* Si la resolución pedida no cabe en la pantalla (por ejemplo, `initWindow(3000, 2000)` en
  un monitor de 1920x1080), la ventana se abre reducida para que se vea completa.
* Las coordenadas del mouse se convierten siempre a coordenadas BGI. Un clic sobre las
  bandas negras se ajusta al borde más cercano del área de dibujo.
* `isResizeEvent()` indica si el usuario cambió el tamaño de la ventana.

> Para que un programa se vea bien en **cualquier** modo gráfico, calcule las coordenadas
> a partir de `getMaxX()` y `getMaxY()` en lugar de usar números fijos (la demostración
> `DemoGraphics` lo hace con las funciones `sx()` y `sy()`).

---

## Diferencias con WinBGI

* **Códigos de color RGB:** se codifican como `0x03RRGGBB` (WinBGI usaba el orden
  BGR de Windows, `0x03BBGGRR`). Si usa `rgb(r, g, b)` no notará la diferencia.
* **Fuentes:** la fuente por defecto (`FontName.DEFAULT`) es una fuente de mapa de bits
  de 8x8 como la de BGI: cada carácter mide `8 * charSize` píxeles. Incluye los 256
  caracteres de la página de códigos 437 de DOS: ASCII, letras acentuadas (á é ñ ü ç ...),
  símbolos (☺ ♥ ♪ ► ↑ ...), líneas para cuadros (│ ┤ ╔ ═ ╬ ...), bloques (░ ▒ ▓ █),
  letras griegas y signos matemáticos (± ≥ ≤ ÷ √ ∞ ...). Las fuentes vectoriales de Borland (`.CHR`: TRIPLEX, SANS_SERIF, GOTHIC, ...) se
  sustituyen por las fuentes lógicas de Java (Serif, SansSerif), rasterizadas sin
  suavizado; para ellas `textWidth`/`textHeight` devuelven las medidas reales de esas fuentes.
  Los caracteres que una fuente no puede dibujar (por ejemplo, los de control) se muestran
  como `?` en lugar de un cuadro vacío.
* **Caracteres de DOS:** en Turbo C un `char` era un byte de la página de códigos 437, pero
  en Java un `char` es Unicode. Al portar programas que escriben códigos numéricos (por
  ejemplo `buffer[0] = 130`), conviértalos con `CodePage437.toUnicode(130)` (devuelve `é`)
  o `CodePage437.decode(bytes)`. Si solo convierte el número a `char`, los códigos 128..159
  quedan como caracteres de control invisibles y los demás no coinciden con los de DOS.
* **`getImage` / `putImage`:** en lugar de un bloque de memoria `void*` se usa el
  objeto inmutable `BgiImage`.
* **`refreshBgi(l, t, r, b)`:** refresca la ventana completa (presentar una textura
  entera es igual de rápido que presentar una región).
* **`printimage`, `installuserdriver`, `registerbgidriver`, `setgraphbufsize`:**
  no se implementaron porque WinBGI tampoco las soportaba o dependen de Windows.
* **Errores de E/S** en `readImageFile`/`writeImageFile` lanzan `UncheckedIOException`
  (y además dejan `graphResult()` en `IO_ERROR`).
* **Hilos:** todas las llamadas deben hacerse desde el mismo hilo (ver
  [Modelo de hilos](#modelo-de-hilos)).

## Ejemplos

Los ejemplos, entre ellos los programas clásicos de Turbo C portados (`BGIDEMO.C`,
`DISPLAY.C`, `FOURIER.C`, `FIRE.C`, `GRAFICA.C`), están en el proyecto hermano
[`mvn-sdl-graphics-examples`](../mvn-sdl-graphics-examples/README.md), que usa esta
biblioteca como dependencia.

## Pruebas

```bash
mvn test
```

| Clase de prueba | Qué verifica |
|-----------------|--------------|
| `RasterizerTest` | Bresenham, relleno de polígonos y elipses, flood fill, viewport y recorte |
| `PaletteTest` | Conversión de colores BGI/RGB, paleta y operación XOR |
| `InputTest` | Teclas extendidas, cola de clics y manejadores del mouse |
| `BitmapFontTest` | Fuente 8x8: tamaño de celda, píxeles de los glifos, escalado, caracteres del español y selección de motor por fuente |
| `ModeResolverTest` | `DETECT`, resolución de cada modo, errores de controlador/modo y rangos |
| `GraphicsWindowTest` | Integración de una ventana completa sobre `FakePlatform`: dibujo, XOR, viewport, texto, imágenes, páginas, eventos, refresco, redimensionado y `setGraphMode` |

## Créditos

Basada en la API de **WinBGIm 6.0** (2004), de Grant Macklem, Gregory Schmelter,
Alan Schmidt, Ivan Stashak y Michael Main (Universidad de Colorado en Boulder),
que a su vez reproduce la *Borland Graphics Interface*.
