package edu.stanford.cs108.bunnyworld;

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;

public class Shape {
    // private final int RECT = 0, TEXT = 1, IMAGE = 2;

    private String name, text;
    private float x, y, width, height;
    private Script script = new Script();
    private boolean visible = true, movable = true, selected = true;
    // private int type = RECT;
    private Bitmap bitmap;
    private int fontSize = 60;
    private int textColor = Color.BLACK;
    private int rectColor = Color.LTGRAY;
    private int selectColor = Color.BLUE;


    // set text style
    private String fontType = "sans-serif";
    private Boolean isBold = false;
    private Boolean isItalic = false;

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getRectColor() {
        return rectColor;
    }

    public void setRectColor(int rectColor) {
        this.rectColor = rectColor;
    }

    public int getSelectColor() {
        return selectColor;
    }

    public void setSelectColor(int selectColor) {
        this.selectColor = selectColor;
    }

    public Shape(String name, String text, float x, float y, float width, float height, boolean selected, Bitmap bitmap) {
        this.name = name;
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.selected = selected;
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmapDrawable(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getFontSize() {
        return this.fontSize;
    }

    public String getText() {
        return text;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public String getName() {
        return this.name;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setInvisible() {
        visible = false;
    }

    public void setVisible() {
        visible = true;
    }

    public boolean isMovable() {
        return movable;
    }

    public void setUnmovable() {
        movable = false;
    }

    public void setMovable() {
        movable = true;
    }

    public void select() {
        selected = true;
    }

    public void unselect() {
        selected = false;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isSelected() {return selected;}

    public Script getScript() { return script; }

    public void setScript(Script script) {
        this.script = script;
    }
    public boolean isClicked(float x, float y) {
        return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
    }

    public void draw(Canvas canvas, boolean editMode) {
        if (editMode || isVisible()) {
            if (text.length() > 0) {
                Paint textPaint = new Paint();
                Rect bounds = new Rect();
                textPaint.setTextSize(fontSize);
                textPaint.getTextBounds(text, 0, text.length(), bounds);
                textPaint.setColor(textColor);

                if (isBold & isItalic) {
                    textPaint.setTypeface(Typeface.create(fontType, Typeface.BOLD_ITALIC));
                }
                else {
                    if (isBold) {
                        textPaint.setTypeface(Typeface.create(fontType, Typeface.BOLD));
                    }
                    if (isItalic) {
                        textPaint.setTypeface(Typeface.create(fontType, Typeface.ITALIC));
                    }
                    if (!isBold & !isItalic) {
                        textPaint.setTypeface(Typeface.create(fontType, Typeface.NORMAL));
                    }
                }

                setWidth(bounds.width());
                setHeight(bounds.height());
//            System.out.println("width: " + width + "; height: " + height);
                canvas.drawText(text, x, y + bounds.height(), textPaint);
            } else if (bitmap != null) {
                canvas.drawBitmap(bitmap, null, new RectF(x, y, x + width, y + height), null);
            } else {
                Paint rectPaint = new Paint();
                rectPaint.setColor(rectColor);
                canvas.drawRect(x, y, x + width, y + height, rectPaint);
            }

            if (selected) {
                Paint selectPaint = new Paint();
                selectPaint.setStyle(Paint.Style.STROKE);
                selectPaint.setColor(selectColor);
                canvas.drawRect(x, y, x + width, y + height, selectPaint);
            }
        }
    }

    public void makeInBound(float screenWidth, float screenHeight) {
        if (x < 0) setX(0);
        if (x + width > screenWidth) setX(screenWidth - width);
        if (y < 0) setY(0);
        if (y + height > screenHeight) setY(screenHeight - height);
    }

    public void makePossessionInBound(float possessionY, boolean inPossession) {
        if (inPossession && y < possessionY) {
            setY(possessionY);
        }
        if (!inPossession && y + height >= possessionY) {
            setY(possessionY - height);
        }
    }

    // TO DO: proportional
    public void scaleInBound(float boundWidth, float boundHeight) {
        if (width > (boundWidth)) {
            setWidth(boundWidth);
//            setX((x1 + x2) / 2);
        }
        if (height > (boundHeight)) {
            setHeight((boundHeight));
//            setY((y1 + y2) / 2);
        }
    }


    // written by ZN Ru
    public void setText(String text) {
        this.text = text;
    }

    // written by ZN Ru
    public void setTextStyle(int fontSize, String fontType, Boolean isBold, Boolean isItalic, int color) {
        this.fontSize = fontSize;
        this.fontType = fontType;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.textColor = color;
    }

    // written by ZN Ru
    public Boolean getIsBold() {return isBold;}

    // written by ZN Ru
    public Boolean getIsItalic() {return isItalic;}

    // written by ZN Ru
    public String getFontType() {return fontType;}



}