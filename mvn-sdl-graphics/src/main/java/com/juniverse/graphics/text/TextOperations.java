package com.juniverse.graphics.text;

import com.juniverse.graphics.dto.TextSettings;

/**
 * Operaciones de texto de BGI.
 */
public interface TextOperations {

    /** Dibuja el texto en la posición actual (y la avanza si la justificación es LEFT horizontal). */
    void outText(String text);

    /** Dibuja el texto en la posición indicada sin modificar la posición actual. */
    void outTextXY(int x, int y, String text);

    /** Ancho en píxeles que ocuparía el texto con la configuración actual. */
    int textWidth(String text);

    /** Alto en píxeles que ocuparía el texto con la configuración actual. */
    int textHeight(String text);

    /** Cambia fuente, dirección y tamaño. */
    void setTextStyle(FontName font, TextDirection direction, int charSize);

    /** Cambia la justificación. */
    void setTextJustify(HorizontalAlignment horizontal, VerticalAlignment vertical);

    /** Define la escala de usuario usada con {@code USER_CHAR_SIZE}. */
    void setUserCharSize(int multX, int divX, int multY, int divY);

    /** Configuración de texto actual. */
    TextSettings textSettings();
}
