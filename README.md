# mvn-graphics

[Read in English](README.en.md)

[![CI](https://github.com/programming0026/mvn-graphics/actions/workflows/ci.yml/badge.svg)](https://github.com/programming0026/mvn-graphics/actions/workflows/ci.yml)

Reescritura en **Java 21** de la biblioteca gráfica **BGI** (`graphics.h`) de Borland
Turbo C / WinBGI, usando **SDL 3** a través de [LWJGL](https://www.lwjgl.org/).

Permite portar casi línea por línea los programas gráficos clásicos de C a Java:

```c
// C / C++ con Borland BGI
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
// Java con SDL Graphics
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

## Contenido del repositorio

| Carpeta | Qué es |
|---------|--------|
| [`mvn-sdl-graphics`](mvn-sdl-graphics/README.md) | La **biblioteca** `com.juniverse.graphics:sdl-graphics`: la API BGI completa sobre SDL 3. No tiene programa principal; se usa como dependencia. |
| [`mvn-sdl-graphics-examples`](mvn-sdl-graphics-examples/README.md) | **Programas de ejemplo**: un menú para elegirlos, una demostración de la biblioteca y programas clásicos de Turbo C portados (la demo oficial de Borland `BGIDEMO.C`, efecto de fuego, series de Fourier, graficadora de funciones y display de 7 segmentos). Genera un jar ejecutable. |

Son dos proyectos Maven independientes: los ejemplos usan la biblioteca instalada en el
repositorio local de Maven.

## Requisitos

| Herramienta | Versión |
|-------------|---------|
| JDK | 21 o superior |
| Maven | 3.9 o superior |
| SDL 3 | No hay que instalarlo: LWJGL trae los binarios nativos para Windows, Linux y macOS |

## Inicio rápido

```bash
# 1. Compilar, probar e instalar la biblioteca
cd mvn-sdl-graphics
mvn clean install

# 2. Compilar los ejemplos y abrir el menú
cd ../mvn-sdl-graphics-examples
mvn clean package
java -jar target/mvn-sdl-graphics-examples-1.0-0-all.jar
```

Desde el menú se elige un ejemplo con las teclas `1` a `6`; `ESC` sale.

> **macOS:** SDL debe ejecutarse en el hilo principal. Use
> `java -XstartOnFirstThread -jar ...`.

## Usar la biblioteca en un proyecto propio

Después de instalarla con `mvn install`, agregue la dependencia:

```xml
<dependency>
    <groupId>com.juniverse.graphics</groupId>
    <artifactId>sdl-graphics</artifactId>
    <version>1.0-0</version>
</dependency>
```

Con esa dependencia llegan LWJGL, SDL 3 y los binarios nativos del sistema operativo
actual. El README de la biblioteca contiene la tabla completa de equivalencias con
`graphics.h`.

## Características

* **API BGI completa:**
  - figuras, rellenos con los 12 patrones de Borland y patrones de usuario,
  - `floodFill`, `getPixel`/`putPixel`, modo XOR,
  - `getImage`/`putImage`,
  - texto con 11 fuentes y justificación,
  - 4 páginas de dibujo con doble búfer,
  - paleta de 16 colores y colores RGB,
  - teclado al estilo `getch`/`kbhit` y mouse.
* **`initGraph(gd, gm, "")`** con los controladores y modos de Borland (`DETECT`, `VGA`,
  `VGAHI`, `EGAHI`, `IBM8514HI`, ...) y `graphResult()`/`graphErrorMsg()`.
* **Ventana redimensionable y maximizable:** el área de dibujo puede crecer con la ventana
  o escalarse conservando la proporción.
* **Fuente de mapa de bits 8x8:** como la de BGI, con los 256 caracteres de la página de
  códigos 437 de DOS, incluidas letras acentuadas, líneas de cuadros, bloques y símbolos.
  `CodePage437` convierte los códigos de caracteres de DOS al portar programas.
* **Lectura y escritura de imágenes:** BMP, PNG, JPG y GIF.
* **Pruebas sin ventanas:** una plataforma falsa en memoria (`FakePlatform`) permite probar
  programas completos en integración continua.

## Arquitectura en breve

* **El dibujo se hace en Java puro** sobre páginas de píxeles en memoria. SDL 3 solo crea
  la ventana, muestra la página visible y entrega los eventos de teclado y mouse. Así
  funcionan exactamente como en BGI `getPixel`, `floodFill`, el modo XOR y `getImage`.
* **Diseño por capas con principios SOLID:**
  - una fachada estática `Bgi` con la API de `graphics.h`,
  - ventanas y ciclo de eventos,
  - operaciones de dibujo y texto,
  - algoritmos de rasterizado,
  - y un puerto `Platform`, cuya única implementación real es la de SDL.
* **Convenciones:**
  - los records van en paquetes `dto` y las excepciones en paquetes `exception`,
  - todas las clases y miembros declaran su modificador de acceso,
  - los mensajes de log y de excepción están en inglés,
  - los comentarios y la documentación, en español.

El detalle de los paquetes, el modelo de hilos y las diferencias con WinBGI está en el
[README de la biblioteca](mvn-sdl-graphics/README.md).

## Integración continua

El flujo [`.github/workflows/ci.yml`](.github/workflows/ci.yml) se ejecuta con cada push o
pull request a `main`, y también se puede lanzar a mano desde la pestaña *Actions*.

* Compila y prueba la biblioteca (`mvn clean install`) y luego los ejemplos
  (`mvn clean verify`).
* Se ejecuta en **Windows, Linux y macOS**: así se comprueba que cada sistema descargue sus
  binarios nativos de SDL 3.
* Las pruebas no abren ventanas, por eso funcionan en los servidores de GitHub.
* Publica como artefactos, uno por sistema operativo, los jars de la biblioteca (clases,
  fuentes, javadoc y pruebas) y el jar ejecutable de los ejemplos. Si alguna prueba falla,
  también publica sus reportes.

## Licencia

Este proyecto se distribuye bajo la [licencia MIT](LICENSE).

## Créditos

Basado en la API de la *Borland Graphics Interface* y de **WinBGIm 6.0** (2004), de Grant
Macklem, Gregory Schmelter, Alan Schmidt, Ivan Stashak y Michael Main (Universidad de
Colorado en Boulder).
