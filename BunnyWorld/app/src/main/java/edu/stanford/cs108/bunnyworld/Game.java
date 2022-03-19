package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;

import java.util.*;

public class Game {

    private String name;
    private Page startPage;
    private List<Page> pages;
    private List<Shape> possession;

    public Game(String name) {
        this.name = name;
        //this.startPage = new Page("page1");
        pages = new ArrayList<>();
        //pages.add(startPage);
        possession = new ArrayList<>();
    }

    public List<Shape> getPossession() {
        return possession;
    }

    public boolean onRemovePossesionShape(Shape shape) {
        int targetIndex = -1;
        for (int i = 0; i < possession.size(); i++) {
            if (possession.get(i).equals(shape)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex == -1) {
            return false;
        }

        possession.remove(targetIndex);
        return true;
    }

    public Shape findPossesionByPosition(float x, float  y) {
        for (int i = possession.size() - 1; i >= 0; i--) {
            if (possession.get(i).isClicked(x, y)) {
                return possession.get(i);
            }
        }
        return null;
    }

    public Page getStartPage() {
        return startPage;
    }

    public void setStartPage(Page startPage) {
        this.startPage = startPage;
    }

    public void addShapeToPossession(Shape shape) {
        possession.add(shape);
    }

    public boolean removeShapeInPossession(Shape shape) { //?shape or shape name
        int targetIndex = -1;
        for (int i = 0; i < possession.size(); i++) {
            if (possession.get(i).equals(shape)) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex == -1) {
            return false;
        }
        possession.remove(targetIndex);
        return true;
    }

    public void addPage(Page page) {
        pages.add(page);
    }

    public boolean removePage(Page page) {
        int targetIndex = -1;
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).equals(page)) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex == -1) {
            return false;
        }
        pages.remove(targetIndex);
        return true;
    }

    public Page findPageByName(String name) {
        for (Page page: pages) {
            if (page.getName().equals((name))) return page;
        }
        return null;
    }

    public Shape findShapeByName(String name) {
        for (Page page: pages) {
            Shape shape = page.findShapeByName(name);
            if (shape != null) return shape;
        }
        return null;
    }

    public void drawPossesion(Canvas canvas) {
        for (Shape shape : possession) {
            shape.draw(canvas, true);
        }
    }

        /*
        ------------------------------- below: Frontend Functions ------------------------------
    */

    // written by ZN Ru
    public int getPageAmount(){
        return pages.size();
    }
    // written by ZN Ru
    public List<Page> getPageList(){
        return pages;
    }

    // written by ZN Ru
    public int getRemovePageIndex(Page page) {
        int targetIndex = -1;
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).equals(page)) {
                targetIndex = i;
                break;
            }
        }
        return targetIndex;
    }
    // written by ZN Ru
    public Page findPageByIndex(int index) {
        if (index < pages.size()){return pages.get(index);}
        return null;
    }
    // written by ZN Ru
    public void setName(String name) {
        this.name = name;
    }

    // written by ZN Ru
    public String getGameName() {
        return name;
    }
}
