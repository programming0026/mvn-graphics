package com.juniverse.graphics.examples.dto;

/**
 * An example of the main menu.
 *
 * @param title       displayed name (the class name)
 * @param description short description (at most about 60 characters)
 * @param entryPoint  action that runs the whole example
 */
public record Example(String title, String description, Runnable entryPoint) {
}
