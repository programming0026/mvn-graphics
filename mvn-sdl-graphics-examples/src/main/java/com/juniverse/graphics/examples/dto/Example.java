package com.juniverse.graphics.examples.dto;

/**
 * Un ejemplo del menú principal.
 *
 * @param title       nombre que se muestra (el de la clase)
 * @param description descripción breve (máximo unos 60 caracteres)
 * @param entryPoint  acción que ejecuta el ejemplo completo
 */
public record Example(String title, String description, Runnable entryPoint) {
}
