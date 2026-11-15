package com.juniverse.graphics.input;

import com.juniverse.graphics.dto.Point;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Estado del mouse de una ventana: posición, clics pendientes y manejadores.
 * <p>
 * Por defecto cada tipo de evento conserva solo el último clic; con
 * {@link #setQueueStatus} pueden encolarse todos (igual que WinBGI).
 */
public final class MouseTracker {

    private final Map<MouseEventKind, Deque<Point>> clicks = new EnumMap<>(MouseEventKind.class);
    private final Map<MouseEventKind, Boolean> queuing = new EnumMap<>(MouseEventKind.class);
    private final Map<MouseEventKind, MouseHandler> handlers = new EnumMap<>(MouseEventKind.class);
    private Point position = new Point(0, 0);

    public MouseTracker() {
        for (MouseEventKind kind : MouseEventKind.values()) {
            clicks.put(kind, new ArrayDeque<>());
            queuing.put(kind, false);
        }
    }

    /** Registra un evento, actualiza la posición e invoca el manejador si existe. */
    public void record(MouseEventKind kind, int x, int y) {
        Deque<Point> queue = clicks.get(kind);
        if (!queuing.get(kind)) {
            queue.clear();
        }
        Point where = new Point(x, y);
        queue.addLast(where);
        position = where;
        MouseHandler handler = handlers.get(kind);
        if (handler != null) {
            handler.handle(x, y);
        }
    }

    /** Coordenada x del mouse (equivale a {@code mousex}). */
    public int x() {
        return position.x();
    }

    /** Coordenada y del mouse (equivale a {@code mousey}). */
    public int y() {
        return position.y();
    }

    /** Indica si hay un evento pendiente de ese tipo (equivale a {@code ismouseclick}). */
    public boolean hasClick(MouseEventKind kind) {
        return !clicks.get(kind).isEmpty();
    }

    /**
     * Extrae el siguiente evento pendiente (equivale a {@code getmouseclick}).
     *
     * @return la posición del evento, o {@link Point#NONE} si no hay ninguno
     */
    public Point pollClick(MouseEventKind kind) {
        Point click = clicks.get(kind).pollFirst();
        return click == null ? Point.NONE : click;
    }

    /** Descarta los eventos pendientes de ese tipo (equivale a {@code clearmouseclick}). */
    public void clearClicks(MouseEventKind kind) {
        clicks.get(kind).clear();
    }

    /** Activa o desactiva el encolado de eventos (equivale a {@code setmousequeuestatus}). */
    public void setQueueStatus(MouseEventKind kind, boolean enabled) {
        queuing.put(Objects.requireNonNull(kind), enabled);
    }

    /** Registra un manejador; {@code null} elimina el existente. */
    public void registerHandler(MouseEventKind kind, MouseHandler handler) {
        if (handler == null) {
            handlers.remove(kind);
        } else {
            handlers.put(kind, handler);
        }
    }
}
