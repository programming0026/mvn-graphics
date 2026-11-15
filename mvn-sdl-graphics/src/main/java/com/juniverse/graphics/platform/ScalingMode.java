package com.juniverse.graphics.platform;

/**
 * Forma en que la página BGI se ajusta a la ventana cuando esta cambia de
 * tamaño (por ejemplo, al maximizarla).
 */
public enum ScalingMode {
    /**
     * El área de dibujo crece con la ventana: al maximizarla, {@code getMaxX()}
     * y {@code getMaxY()} pasan a ser el tamaño real de la ventana y no se
     * escala nada (el texto se ve nítido). El área nunca es menor que la
     * resolución inicial; si la ventana es más pequeña, la imagen se reduce
     * conservando la proporción. Lo ya dibujado se conserva arriba a la
     * izquierda. Es el modo por defecto.
     */
    RESIZE,
    /**
     * Conserva la proporción. El espacio sobrante se rellena con el color de
     * fondo actual (el de {@code setBkColor}).
     */
    KEEP_ASPECT,
    /** Estira la imagen hasta cubrir toda la ventana, aunque se deforme. */
    STRETCH,
    /**
     * Amplía solo por factores enteros (x2, x3, ...), de modo que cada píxel
     * BGI ocupa exactamente el mismo número de píxeles de pantalla. Es el
     * modo más nítido para texto y líneas; el resto se rellena con el color
     * de fondo.
     */
    INTEGER
}
