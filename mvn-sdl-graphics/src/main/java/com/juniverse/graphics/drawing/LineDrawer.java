package com.juniverse.graphics.drawing;

import com.juniverse.graphics.dto.Point;

import java.util.List;

/**
 * Implementation of {@link LineOperations} on a {@link DrawingContext}.
 */
public final class LineDrawer implements LineOperations {

    private final DrawingContext context;
    private final StrokePainter stroke;

    public LineDrawer(DrawingContext context) {
        this.context = context;
        this.stroke = new StrokePainter(context);
    }

    @Override
    public void line(int x1, int y1, int x2, int y2) {
        stroke.strokePolyline(List.of(new Point(x1, y1), new Point(x2, y2)));
        context.changed();
    }

    @Override
    public void lineTo(int x, int y) {
        Point from = context.state().position();
        line(from.x(), from.y(), x, y);
        moveTo(x, y);
    }

    @Override
    public void lineRel(int dx, int dy) {
        Point from = context.state().position();
        lineTo(from.x() + dx, from.y() + dy);
    }

    @Override
    public void moveTo(int x, int y) {
        context.state().setPosition(new Point(x, y));
    }

    @Override
    public void moveRel(int dx, int dy) {
        context.state().setPosition(context.state().position().translate(dx, dy));
    }

    @Override
    public int getX() {
        return context.state().position().x();
    }

    @Override
    public int getY() {
        return context.state().position().y();
    }
}
