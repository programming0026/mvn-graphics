package com.juniverse.graphics.text;

/**
 * Conversión de la página de códigos 437 (el juego de caracteres de DOS y de
 * Turbo C) a Unicode.
 * <p>
 * En los programas BGI originales un {@code char} era un byte: los códigos
 * 128..255 eran letras acentuadas, líneas para dibujar cuadros (░▒▓│┤...) y
 * símbolos matemáticos, y los códigos 1..31 tenían dibujos propios (☺♥♪...).
 * En Java, convertir directamente esos números a {@code char} da caracteres
 * Unicode distintos (por ejemplo, 128..159 son caracteres de control
 * invisibles). Esta clase devuelve el carácter que se veía en DOS.
 */
public final class CodePage437 {

    /** Códigos 0x00..0x1F: dibujos que la pantalla de DOS mostraba para los caracteres de control. */
    private static final String LOW_GLYPHS =
            " ☺☻♥♦♣♠•◘○◙♂♀♪♫☼"
            + "►◄↕‼¶§▬↨↑↓→←∟↔▲▼";

    /** Código 0x7F (DEL) en DOS. */
    private static final char DELETE_GLYPH = '⌂';

    /**
     * Espacio no separable (código 0xFF de DOS, U+00A0). Se escribe por su
     * código para que no quede un espacio invisible dentro de un literal.
     */
    public static final char NO_BREAK_SPACE = (char) 0x00A0;

    /** Códigos 0x80..0xFF. */
    private static final String HIGH_GLYPHS =
            "ÇüéâäàåçêëèïîìÄÅ"
            + "ÉæÆôöòûùÿÖÜ¢£¥₧ƒ"
            + "áíóúñÑªº¿⌐¬½¼¡«»"
            + "░▒▓│┤╡╢╖╕╣║╗╝╜╛┐"
            + "└┴┬├─┼╞╟╚╔╩╦╠═╬╧"
            + "╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀"
            + "αßΓπΣσµτΦΘΩδ∞φε∩"
            + "≡±≥≤⌠⌡÷≈°∙·√ⁿ²■" + NO_BREAK_SPACE;

    private static final int CONTROL_LIMIT = 0x20;
    private static final int DELETE = 0x7F;
    private static final int HIGH_START = 0x80;
    private static final int BYTE_MASK = 0xFF;

    private CodePage437() {
    }

    /**
     * Carácter Unicode que DOS mostraba para el código indicado. Solo se usan
     * los 8 bits inferiores, igual que con un {@code char} de C.
     */
    public static char toUnicode(int code) {
        int value = code & BYTE_MASK;
        if (value < CONTROL_LIMIT) {
            return LOW_GLYPHS.charAt(value);
        }
        if (value == DELETE) {
            return DELETE_GLYPH;
        }
        if (value >= HIGH_START) {
            return HIGH_GLYPHS.charAt(value - HIGH_START);
        }
        return (char) value;
    }

    /** Convierte una secuencia de bytes de DOS (por ejemplo, una cadena de C) a texto Unicode. */
    public static String decode(byte[] bytes) {
        StringBuilder text = new StringBuilder(bytes.length);
        for (byte value : bytes) {
            text.append(toUnicode(value));
        }
        return text.toString();
    }
}
