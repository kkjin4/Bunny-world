package edu.stanford.cs108.bunnyworld;
import android.graphics.Canvas;

import java.util.*;


public class Page {

    private Canvas canvas;
    private String name;
    private List<Shape> shapes;
    private Shape selected;

    public Shape getSelected() {
        return selected;
    }

    public void setSelected(Shape selected) {
        this.selected = selected;
    }

    public Page(String name) {
        this.name = name;
        shapes = new ArrayList<>();
        canvas = new Canvas();
    }

    public void onAdd(Shape shape) {
        shapes.add(shape);
    }

    public void onRename(String name) {
        this.name = name;
    }

    public boolean onRemove(Shape shape) { //?shape or shape name
        int targetIndex = -1;
        for (int i = 0; i < shapes.size(); i++) {
            if (shapes.get(i).equals(shape)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex == -1) {
            return false;
        }

        shapes.remove(targetIndex);
        return true;
    }

    public void draw(Canvas canvas, boolean editMode) {

        for (Shape shape : shapes) {
            shape.draw(canvas, editMode);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Shape> getShapes() {
        return shapes;
    }

    public Shape findShapeByPosition(float x, float y, Shape currentShape, boolean editMode) {
        for (int i = shapes.size() - 1; i >= 0; i--) {
            Shape shape = shapes.get(i);

            if (shape.isClicked(x,y) && (shape.isVisible() || editMode) &&
                    (currentShape == null || !shape.getName().equals(currentShape.getName()))) {
                return shape;
            }
        }
        return null;
    }

    public Shape findShapeByName(String name) {
        for (Shape shape: shapes) {
            if (shape.getName().equals(name)) return shape;
        }
        return null;
    }

    // written by ZN Ru
    public Canvas getCanvas() {
        return canvas;
    }

    // written by ZN Ru
    public int getShapeAmount() {
        return shapes.size();
    }
}

