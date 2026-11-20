package com.juniverse.graphics.input;

import com.juniverse.graphics.dto.Point;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Mouse state of a window: position, pending clicks and handlers.
 * <p>
 * By default each event kind keeps only the last click; with
 * {@link #setQueueStatus} all of them can be queued (same as WinBGI).
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

    /** Records an event, updates the position and invokes the handler if there is one. */
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

    /** Mouse x coordinate (equivalent to {@code mousex}). */
    public int x() {
        return position.x();
    }

    /** Mouse y coordinate (equivalent to {@code mousey}). */
    public int y() {
        return position.y();
    }

    /** Tells whether there is a pending event of that kind (equivalent to {@code ismouseclick}). */
    public boolean hasClick(MouseEventKind kind) {
        return !clicks.get(kind).isEmpty();
    }

    /**
     * Takes the next pending event (equivalent to {@code getmouseclick}).
     *
     * @return the position of the event, or {@link Point#NONE} if there is none
     */
    public Point pollClick(MouseEventKind kind) {
        Point click = clicks.get(kind).pollFirst();
        return click == null ? Point.NONE : click;
    }

    /** Discards the pending events of that kind (equivalent to {@code clearmouseclick}). */
    public void clearClicks(MouseEventKind kind) {
        clicks.get(kind).clear();
    }

    /** Enables or disables event queueing (equivalent to {@code setmousequeuestatus}). */
    public void setQueueStatus(MouseEventKind kind, boolean enabled) {
        queuing.put(Objects.requireNonNull(kind), enabled);
    }

    /** Registers a handler; {@code null} removes the existing one. */
    public void registerHandler(MouseEventKind kind, MouseHandler handler) {
        if (handler == null) {
            handlers.remove(kind);
        } else {
            handlers.put(kind, handler);
        }
    }
}
