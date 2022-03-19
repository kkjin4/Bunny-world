package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaActionSound;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageView extends View{
    private boolean editMode = false;// judge edit or play

    private float x1, y1, x2, y2, oldx, oldy;

    private Game game;
    private Page page;

    private float possessionY;
    private float screenWidth;
    private float screenHeight;

    private Shape selectedShape;

    private boolean active = false; // whether the game is in play mode and should execute the scripts

    private Map<String, Integer> mediaMap = new HashMap<>();
    private Map<String, Integer> drawableMap = new HashMap<>();

    private TextView xPosET;
    private TextView yPosET;
    private TextView widthET;
    private TextView heightET;

    private EditText pageNameET;
    private EditText shapeNameET;

    private Spinner pageListSpinner;
    private Spinner shapeListSpinner;

    private Switch resizeSwitch;

    private CheckBox movableCheckBox;
    private CheckBox visibleCheckBox;

    public void init(Game game) {
        //game and page need to be initialized depending on edit/new/play
        if (game == null) System.out.println("???");
        initMediaMap();
        initDrawableMap();
        getScreenDimensions();

        handleOnEnter();


    }


    private void initMediaMap() {
        mediaMap.put("carrotcarrotcarrot", R.raw.carrotcarrotcarrot);
        mediaMap.put("evillaugh", R.raw.evillaugh);
        mediaMap.put("fire", R.raw.fire);
        mediaMap.put("hooray", R.raw.hooray);
        mediaMap.put("munch", R.raw.munch);
        mediaMap.put("munching", R.raw.munching);
        mediaMap.put("woof", R.raw.woof);
    }

    private void initDrawableMap() {
        drawableMap.put("rect", R.drawable.rect);
        drawableMap.put("carrot", R.drawable.carrot);
        drawableMap.put("carrot2", R.drawable.carrot2);
        drawableMap.put("death", R.drawable.death);
        drawableMap.put("duck", R.drawable.duck);
        drawableMap.put("fire", R.drawable.fire);
        drawableMap.put("mystic", R.drawable.mystic);
        drawableMap.put("text", R.drawable.text);
    }

    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(game);
        selectedShape = null;
    }


    private Shape clickedShape;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Boolean resize = false;
        if (editMode) {
            resizeSwitch = (Switch)  ((Activity) getContext()).findViewById(R.id.resizeSwitch);
            resize = resizeSwitch.isChecked();
        }

        // normal mode, not resize
        if (!resize) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    y1 = event.getY();
                    clickedShape = page.findShapeByPosition(x1, y1, null, editMode);
//                System.out.println("clickedShape: " + (clickedShape == null ? "null" : clickedShape.getName()));
                    if (clickedShape != null) {
                        handleOnClick(clickedShape);
                    } else {
                        clickedShape = game.findPossesionByPosition(x1, y1);
                    }
                    unSelectShape();
                    setSelectShape(clickedShape);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:

                    if (clickedShape != null) {
                        x2 = event.getX();
                        y2 = event.getY();
                        if (clickedShape.isMovable()) {
                            clickedShape.setX(x2 - clickedShape.getWidth() / 2);
                            clickedShape.setY(y2 - clickedShape.getHeight() / 2);

                            // update position display
                            if (editMode) {
                                xPosET = (TextView) ((Activity) getContext()).findViewById(R.id.xPositionInput);
                                yPosET = (TextView) ((Activity) getContext()).findViewById(R.id.yPositionInput);

                                xPosET.setText(new DecimalFormat("0").format(clickedShape.getX()));
                                yPosET.setText(new DecimalFormat("0").format(clickedShape.getY()));
                            }
                            if (isClickInPossession(y2)) {
                                if (page.onRemove(clickedShape)) {
                                    game.getPossession().add(clickedShape);
                                }
                                clickedShape.scaleInBound(screenWidth, screenHeight - possessionY);
                                clickedShape.makePossessionInBound(possessionY, true);
                            } else {
                                if (game.onRemovePossesionShape(clickedShape)) {
                                    page.onAdd(clickedShape);
                                }
                                clickedShape.scaleInBound(screenWidth, possessionY);
                                clickedShape.makePossessionInBound(possessionY, false);
                                Shape bottomShape = page.findShapeByPosition(x2, y2, clickedShape, editMode);
                                if (clickedShape.isMovable()) {
                                    handleOnDrop(clickedShape, bottomShape);
                                }

                            }

                            clickedShape.makeInBound(screenWidth, screenHeight);
                            clickedShape = null;
                        }
                    }
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (clickedShape != null) {
                        x2 = event.getX();
                        y2 = event.getY();
                        if (clickedShape.isMovable()) {
                            clickedShape.setX(x2 - clickedShape.getWidth() / 2);
                            clickedShape.setY(y2 - clickedShape.getHeight() / 2);

                            // update position display
                            if (editMode) {
                                xPosET = (TextView) ((Activity) getContext()).findViewById(R.id.xPositionInput);
                                yPosET = (TextView) ((Activity) getContext()).findViewById(R.id.yPositionInput);

                                xPosET.setText(new DecimalFormat("0").format(clickedShape.getX()));
                                yPosET.setText(new DecimalFormat("0").format(clickedShape.getY()));
                            }
                        }
                        invalidate();
                    }
                    break;
            }
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    y1 = event.getY();
                    clickedShape = page.findShapeByPosition(x1, y1, null, editMode);
                    if(clickedShape != null) {
                        oldx = clickedShape.getX();
                        oldy = clickedShape.getY();
                    }
//                System.out.println("clickedShape: " + (clickedShape == null ? "null" : clickedShape.getName()));

                    unSelectShape();
                    setSelectShape(clickedShape);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:

                    if (clickedShape != null) {
                        x2 = event.getX();
                        y2 = event.getY();

                        if (clickedShape.isMovable()) {
                            if (isClickInPossession(y2)) {
                                y2 = possessionY;
                            }

                            clickedShape.setX(Math.min(oldx,x2));
                            clickedShape.setY(Math.min(oldy,y2));

                            clickedShape.setWidth(Math.abs(x2 - oldx));
                            clickedShape.setHeight(Math.abs(y2 - oldy));

                            // update size display
                            if (editMode) {
                                widthET = (TextView) ((Activity) getContext()).findViewById(R.id.widthInput);
                                heightET = (TextView) ((Activity) getContext()).findViewById(R.id.heightInput);

                                widthET.setText(new DecimalFormat("0").format(clickedShape.getWidth()));
                                heightET.setText(new DecimalFormat("0").format(clickedShape.getHeight()));
                            }
                        }
                    }
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (clickedShape != null) {
                        x2 = event.getX();
                        y2 = event.getY();
                        if (clickedShape.isMovable()) {
                            if (isClickInPossession(y2)) {
                                y2 = possessionY;
                            }

                            clickedShape.setX(Math.min(oldx,x2));
                            clickedShape.setY(Math.min(oldy,y2));

                            clickedShape.setWidth(Math.abs(x2 - oldx));
                            clickedShape.setHeight(Math.abs(y2 - oldy));

                            // update size display
                            if (editMode) {
                                widthET = (TextView) ((Activity) getContext()).findViewById(R.id.widthInput);
                                heightET = (TextView) ((Activity) getContext()).findViewById(R.id.heightInput);

                                widthET.setText(new DecimalFormat("0").format(clickedShape.getWidth()));
                                heightET.setText(new DecimalFormat("0").format(clickedShape.getHeight()));
                            }
                        }
                        invalidate();
                    }
                    break;
            }
        }
        return true;
    }

    private final int NAVIGATION_BAR_HEIGHT = 178;
    private void getScreenDimensions() {
//        WindowManager windowManager = (WindowManager) ((Activity)getContext()).getSystemService(Context.WINDOW_SERVICE);
//        Display display = windowManager.getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels - NAVIGATION_BAR_HEIGHT;
    }

    private boolean isClickInPossession(float y) {
        return y >= possessionY;
    }

    private void handleOnClick(Shape shape) {
        if (shape == null || !active) return;
        List<Action> actions = shape.getScript().getClickAction();
        for (Action action : actions) {
            handleAction(action);
        }
    }

    private void handleOnEnter() {
        if (active) {
            for (Shape shape : page.getShapes()) {
                List<Action> actions = shape.getScript().getEnterAction();
                for (Action action : actions) {
                    handleAction(action);
                }
            }
        }
    }

    private void handleOnDrop(Shape topShape, Shape bottomShape) {
        if (bottomShape == null || topShape == null || !active) {
            return;
        }
        Map<String, List<Action>> actionMap = bottomShape.getScript().getDropActions();
        if (actionMap.containsKey(topShape.getName())) {
            List<Action> actions = actionMap.get(topShape.getName());
            for (Action action : actions) {
                handleAction(action);
            }
        }
    }

    private void handleAction(Action action) {
        if (action == null) return;
        String verb = action.getVerb(), modifier = action.getModifier();

        switch (verb) {
            case "goto":
                Page gotoPage = game.findPageByName(modifier);
                if (gotoPage == null) {
                    return;
                }
                page = gotoPage;
                handleOnEnter();
                // error check if gotoPage == null (e.g. toast)
                if (editMode) {
                    pageNameET = (EditText) ((Activity) getContext()).findViewById(R.id.pageNameInput);
                    pageNameET.setText(page.getName());
                    pageListSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.pageNameSpinner);
                    int pageAdapter = pageListSpinner.getAdapter().getCount();
                    for (int pos = 0; pos < pageAdapter; pos++) {
                        if (pageListSpinner.getItemAtPosition(pos).toString().equals(page.getName())) {
                            pageListSpinner.setSelection(pos);
                            break;
                        }
                    }
                }

                invalidate();
                break;
            case "play":
                if (mediaMap.containsKey(modifier)) {
                    MediaPlayer mp = MediaPlayer.create(getContext(), mediaMap.get(modifier));
                    mp.start();
                }
                break;
            case "hide":
                Shape hideShape = game.findShapeByName(modifier);
                if (hideShape == null) {
                    return;
                }
                hideShape.setInvisible();
                invalidate();
                break;
            case "show":
                Shape showShape = game.findShapeByName(modifier);
                if (showShape == null) {
                    return;
                }
                showShape.setVisible();
                invalidate();
                break;
        }
    }

    private void setSelectShape(Shape shape) {
        if (shape != null)  {
            shape.select();
            selectedShape = shape;

            if (editMode) {

                shapeNameET = (EditText) ((Activity) getContext()).findViewById(R.id.shapeNameInput);
                xPosET = (TextView) ((Activity) getContext()).findViewById(R.id.xPositionInput);
                yPosET = (TextView) ((Activity) getContext()).findViewById(R.id.yPositionInput);
                widthET = (TextView) ((Activity) getContext()).findViewById(R.id.widthInput);
                heightET = (TextView) ((Activity) getContext()).findViewById(R.id.heightInput);

                shapeNameET.setText(selectedShape.getName());
                xPosET.setText(new DecimalFormat("0").format(selectedShape.getX()));
                yPosET.setText(new DecimalFormat("0").format(selectedShape.getY()));
                widthET.setText(new DecimalFormat("0").format(selectedShape.getWidth()));
                heightET.setText(new DecimalFormat("0").format(selectedShape.getHeight()));

                movableCheckBox = (CheckBox) ((Activity) getContext()).findViewById(R.id.movableCheckBox);
                visibleCheckBox = (CheckBox) ((Activity) getContext()).findViewById(R.id.visibleCheckBox);

                movableCheckBox.setChecked(selectedShape.isMovable());
                visibleCheckBox.setChecked(selectedShape.isVisible());

                shapeListSpinner = (Spinner) ((Activity) getContext()).findViewById(R.id.shapeFigureSpinner);
                if (selectedShape.getBitmap() != null) {
                    String shapeType0;
                    String shapeType1;
                    String shapeType;
                    int shapeAdapter = shapeListSpinner.getAdapter().getCount();
                    for (int pos = 1; pos < shapeAdapter; pos++) {
                        shapeType0 = shapeListSpinner.getItemAtPosition(pos).toString();
                        shapeType1 = shapeType0.substring(shapeType0.indexOf(","), shapeType0.indexOf("}"));
                        shapeType = shapeType1.substring(shapeType1.indexOf("=") + 1);
                        if (((BitmapDrawable) getResources().getDrawable(drawableMap.get(shapeType))).getBitmap().sameAs(selectedShape.getBitmap())) {
                            shapeListSpinner.setSelection(pos);
                            break;
                        }
                    }
                }
                else {
                    if (selectedShape.getText().equals("")) {
                        shapeListSpinner.setSelection(1);
                    }
                    else {
                        shapeListSpinner.setSelection(shapeListSpinner.getAdapter().getCount() - 1);
                    }
                }
            }

        }
    }

    private void unSelectShape() {
        if (selectedShape != null) {
            selectedShape.unselect();
            selectedShape = null;

            if (editMode) {
                shapeNameET = (EditText) ((Activity) getContext()).findViewById(R.id.shapeNameInput);
                xPosET = (TextView) ((Activity) getContext()).findViewById(R.id.xPositionInput);
                yPosET = (TextView) ((Activity) getContext()).findViewById(R.id.yPositionInput);
                widthET = (TextView) ((Activity) getContext()).findViewById(R.id.widthInput);
                heightET = (TextView) ((Activity) getContext()).findViewById(R.id.heightInput);

                shapeNameET.setText("");
                xPosET.setText("");
                yPosET.setText("");
                widthET.setText("");
                heightET.setText("");

                movableCheckBox = (CheckBox) ((Activity) getContext()).findViewById(R.id.movableCheckBox);
                visibleCheckBox = (CheckBox) ((Activity) getContext()).findViewById(R.id.visibleCheckBox);

                movableCheckBox.setChecked(true);
                visibleCheckBox.setChecked(true);
            }
        }
    }


    private static int MAX_NUM_POSSESSIONS = 10;
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        page.draw(canvas, editMode);

        // Automatically adjust height and position in possession
        getScreenDimensions();
        float possessionShapeWidth = screenWidth / MAX_NUM_POSSESSIONS;
        float possessionShapeHeight = (screenHeight - possessionY) / 2;

        for (int i = 0; i < game.getPossession().size(); i++) {
            Shape currShape = game.getPossession().get(i);
            currShape.setWidth(possessionShapeWidth);
            currShape.setHeight(possessionShapeHeight);
            currShape.setX((i % MAX_NUM_POSSESSIONS) * possessionShapeWidth);
            currShape.setY(possessionY + (possessionShapeHeight * (i / MAX_NUM_POSSESSIONS)));
            currShape.draw(canvas, editMode);
        }
    }


    /*
    ------------------------------- below: Frontend Functions ------------------------------
    */
    // written by ZN Ru
    public Shape getSelectedShape() {
        for (Shape shape:page.getShapes()){
            if (shape.isSelected()) {return shape;}
        }
        return null;
    }

    public void drawPage(Page page) {
        this.page = page;
        invalidate();
    }

    public void importGame(Game game){
        this.game = game;
    }

    public Page getPage() {
        return page;
    }

    public void setEditMode(Boolean editMode){
        this.editMode = editMode;
    }

    public void setPossessionY(int possessionY){
        this.possessionY = possessionY;
    }
    public void setActive(Boolean active) {this.active = active;}
}


