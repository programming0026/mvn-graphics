package com.juniverse.graphics.text;

/**
 * Conversion of code page 437 (the character set of DOS and
 * Turbo C) to Unicode.
 * <p>
 * In the original BGI programs a {@code char} was a byte: codes
 * 128..255 were accented letters, box-drawing lines (░▒▓│┤...) and
 * mathematical symbols, and codes 1..31 had their own glyphs (☺♥♪...).
 * In Java, directly converting those numbers to {@code char} gives different
 * Unicode characters (for example, 128..159 are invisible control
 * characters). This class returns the character that was seen in DOS.
 */
public final class CodePage437 {

    /** Codes 0x00..0x1F: glyphs that the DOS screen showed for control characters. */
    private static final String LOW_GLYPHS =
            " ☺☻♥♦♣♠•◘○◙♂♀♪♫☼"
            + "►◄↕‼¶§▬↨↑↓→←∟↔▲▼";

    /** Code 0x7F (DEL) in DOS. */
    private static final char DELETE_GLYPH = '⌂';

    /**
     * Non-breaking space (DOS code 0xFF, U+00A0). It is written by its
     * code so that no invisible space is left inside a literal.
     */
    public static final char NO_BREAK_SPACE = (char) 0x00A0;

    /** Codes 0x80..0xFF. */
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
     * Unicode character that DOS showed for the given code. Only the
     * lower 8 bits are used, same as with a C {@code char}.
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

    /** Converts a sequence of DOS bytes (for example, a C string) to Unicode text. */
    public static String decode(byte[] bytes) {
        StringBuilder text = new StringBuilder(bytes.length);
        for (byte value : bytes) {
            text.append(toUnicode(value));
        }
        return text.toString();
    }
}
