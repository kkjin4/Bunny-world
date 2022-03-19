package edu.stanford.cs108.bunnyworld;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.os.Bundle;
import android.widget.*;
import android.database.*;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.*;


public class EditGameActivity extends AppCompatActivity {
    final Boolean EDIT_MODE = true;

    private Spinner pageListSpinner;

    private Spinner shapeListSpinner;

    private Spinner triggerListSpinner;
    private Spinner shapeTypeListSpinner;
    private Spinner actionListSpinner;
    private Spinner objectListSpinner;

    private Map<String, Integer> drawableMap = new HashMap<>();

    private PageView pageView;

    private DrawerLayout drawerLayout;
    private RelativeLayout setView;

    // database related
    BunnyDB db = new BunnyDB(this);

    // new one or edit current game
    // if this is a new game
    private Game game;
    private Page currentPage;
    private Shape currentShape = null;

    int pageAddCount;
    int shapeAddCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_game_drawerlayout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// app start in landscape view

        String gameName = getIntent().getStringExtra("gameName");

        if (getIntent().getBooleanExtra("newGame", false)) {
            game = new Game(gameName);
            currentPage = new Page("page1");
            game.addPage(currentPage);
            game.setStartPage(currentPage);
        }
        else {
            game = db.getGameByName(gameName);

            if (game == null) {
                String gameName1 = "NewGame1";
                int count = 1;
                while (db.getGameByName(gameName1) != null) {
                    count++;
                    gameName1 = "NewGame" + Integer.toString(count);
                }
                game = new Game(gameName1);
                currentPage = new Page("page1");
                game.addPage(currentPage);
                game.setStartPage(currentPage);
            }
        }



        pageAddCount = game.getPageAmount();
        shapeAddCount = game.getPossession().size();
        for (Page page:game.getPageList()) {
            shapeAddCount = shapeAddCount + page.getShapeAmount();
        }

        EditText gameNameET = (EditText) findViewById(R.id.gameNameInput);
        gameNameET.setText(game.getGameName());

        // change start page display
        currentPage = game.getStartPage();
        TextView startPageET = (TextView) findViewById(R.id.starPageNameTV);
        startPageET.setText(currentPage.getName());
        startPageET.setTextColor(Color.BLUE);

        pageView = (PageView) findViewById(R.id.editPageView);
        pageView.importGame(game);
        pageView.setEditMode(EDIT_MODE);
        pageView.drawPage(currentPage);

        initPageNameSpinner();

        // drawer layout operations
        initDrawerLayout();

        initDrawableMap();

        // page spinner
        // jump to selected page
        jumpToPage();

        // shape spinner
        // show and select the shape want to add
        initShapeSpinner();

        // script spinner
        // update with different choice
        initScriptSpinner();
        updateScriptSpinner();

        // listen to resize switch
        listenResizeSwitch();

    }

    // set new possession view height
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        View separateLine = (View) findViewById(R.id.separateLine);
        int[] possessionXY =  new int[2];
        separateLine.getLocationOnScreen(possessionXY);
        int possessionY = possessionXY[1];

        pageView = (PageView) findViewById(R.id.editPageView);
        possessionY = separateLine.getTop();
        pageView.setPossessionY(possessionY);
    }


    public void initDrawerLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.editDrawerLayout);
        setView = (RelativeLayout) findViewById(R.id.setView);

        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {}

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {}

            @Override
            public void onDrawerStateChanged(int newState) {}
        });
    }

    public void initPageNameSpinner() {

        EditText pageNameET = (EditText) findViewById(R.id.pageNameInput);
        pageNameET.setText(currentPage.getName());
        pageListSpinner = (Spinner) findViewById(R.id.pageNameSpinner);

        ArrayList<String> currentPageList = new ArrayList<>();
        for (int i = 0; i < game.getPageAmount(); i++) {
            currentPageList.add(game.findPageByIndex(i).getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currentPageList);
        pageListSpinner.setAdapter(adapter);

        // set spinner show current page
        // set start page in different color
        Page startPage = game.getStartPage();


        int pageAdapter = pageListSpinner.getAdapter().getCount();
        for (int pos = 0; pos < pageAdapter; pos++) {
            if (pageListSpinner.getItemAtPosition(pos).toString().equals(currentPage.getName())) {
                pageListSpinner.setSelection(pos);
                break;
            }
        }
    }

    // jump to the page touched in spinner
    public void jumpToPage() {
        pageListSpinner = (Spinner) findViewById(R.id.pageNameSpinner);
        ArrayList<String> currentPageList = new ArrayList<>();
        for (int i = 0; i < game.getPageAmount(); i++) {
            currentPageList.add(game.findPageByIndex(i).getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currentPageList);
        pageListSpinner.setAdapter(adapter);

        int newPosition = adapter.getPosition(currentPage.getName());
        pageListSpinner.setSelection(newPosition);

        pageListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedPageName = pageListSpinner.getSelectedItem().toString();
                currentPage = game.findPageByName(selectedPageName);

                pageView = (PageView) findViewById(R.id.editPageView);
                pageView.drawPage(currentPage);
                Shape selectedShape = pageView.getSelectedShape();
                if (selectedShape != null) {
                    selectedShape.unselect();
                    selectedShape = null;

                    TextView shapeNameET = (TextView) findViewById(R.id.shapeNameInput);
                    TextView xPosET = (TextView) findViewById(R.id.xPositionInput);
                    TextView yPosET = (TextView) findViewById(R.id.yPositionInput);
                    TextView widthET = (TextView) findViewById(R.id.widthInput);
                    TextView heightET = (TextView) findViewById(R.id.heightInput);

                    shapeNameET.setText("");
                    xPosET.setText("");
                    yPosET.setText("");
                    widthET.setText("");
                    heightET.setText("");

                    CheckBox movableCheckBox = (CheckBox) findViewById(R.id.movableCheckBox);
                    CheckBox visibleCheckBox = (CheckBox) findViewById(R.id.visibleCheckBox);

                    movableCheckBox.setChecked(true);
                    visibleCheckBox.setChecked(true);
                }

                int newPosition = adapter.getPosition(selectedPageName);
                pageListSpinner.setSelection(newPosition);

                EditText pageNameET = (EditText) findViewById(R.id.pageNameInput);
                pageNameET.setText(currentPage.getName());

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


    // show bitmap and text at the same time in shape spinner
    public void initShapeSpinner() {
        shapeListSpinner = (Spinner) findViewById(R.id.shapeFigureSpinner);

        SimpleAdapter adapter = new SimpleAdapter(this, getShapeSpinnerList(), R.layout.shape_spinner_layout, new String[]{"bitmap","bitmapName"}, new int[]{R.id.shapeBitmap, R.id.shapeBitmapName});

        shapeListSpinner.setAdapter(adapter);
    }

    public ArrayList<Map<String, Object>> getShapeSpinnerList() {
        ArrayList<Map<String, Object>> shapeList = new ArrayList<>();

        HashMap<String, Object> item0 = new HashMap<>();
        item0.put("bitmap", R.drawable.blank);
        item0.put("bitmapName", "SelectShape");
        shapeList.add(item0);

        HashMap<String, Object> item1 = new HashMap<>();
        item1.put("bitmap", R.drawable.rect);
        item1.put("bitmapName", "rect");
        shapeList.add(item1);

        HashMap<String, Object> item2 = new HashMap<>();
        item2.put("bitmap", R.drawable.carrot);
        item2.put("bitmapName", "carrot");
        shapeList.add(item2);

        HashMap<String, Object> item3 = new HashMap<>();
        item3.put("bitmap", R.drawable.carrot2);
        item3.put("bitmapName", "carrot2");
        shapeList.add(item3);

        HashMap<String, Object> item4 = new HashMap<>();
        item4.put("bitmap", R.drawable.death);
        item4.put("bitmapName", "death");
        shapeList.add(item4);

        HashMap<String, Object> item5 = new HashMap<>();
        item5.put("bitmap", R.drawable.duck);
        item5.put("bitmapName", "duck");
        shapeList.add(item5);

        HashMap<String, Object> item6 = new HashMap<>();
        item6.put("bitmap", R.drawable.fire);
        item6.put("bitmapName", "fire");
        shapeList.add(item6);

        HashMap<String, Object> item7 = new HashMap<>();
        item7.put("bitmap", R.drawable.mystic);
        item7.put("bitmapName", "mystic");
        shapeList.add(item7);

        HashMap<String, Object> item8 = new HashMap<>();
        item8.put("bitmap", R.drawable.text);
        item8.put("bitmapName", "text");
        shapeList.add(item8);

        return shapeList;
    }



    // script spinner update helper function
    public void initScriptSpinner() {
        triggerListSpinner = (Spinner) findViewById(R.id.triggerListSpinner);
        shapeTypeListSpinner = (Spinner) findViewById(R.id.shapeTypeListSpinner);
        actionListSpinner = (Spinner) findViewById(R.id.actionListSpinner);
        objectListSpinner = (Spinner) findViewById(R.id.objectListSpinner);

        String[] currentTrigger = {"trigger",  "onClick", "onEnter", "onDrop"};
        ArrayAdapter<String> triggerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currentTrigger);

        String[] currentShapeTypeList = {"shape"};
        ArrayAdapter<String> shapeTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currentShapeTypeList);

        String[] currentActionList = {"action"};
        ArrayAdapter<String> actionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currentActionList);

        String[] currentObjectList = {"object"};
        ArrayAdapter<String> objectAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currentObjectList);

        triggerListSpinner.setAdapter(triggerAdapter);
        shapeTypeListSpinner.setAdapter(shapeTypeAdapter);
        actionListSpinner.setAdapter(actionAdapter);
        objectListSpinner.setAdapter(objectAdapter);
    }

    public void updateScriptSpinner() {
        triggerListSpinner = (Spinner) findViewById(R.id.triggerListSpinner);
        shapeTypeListSpinner = (Spinner) findViewById(R.id.shapeTypeListSpinner);
        actionListSpinner = (Spinner) findViewById(R.id.actionListSpinner);
        objectListSpinner = (Spinner) findViewById(R.id.objectListSpinner);

        triggerListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedTrigger = triggerListSpinner.getSelectedItem().toString();
                if (selectedTrigger.equals("trigger")) {
                    initScriptSpinner();
                }
                if (selectedTrigger.equals("onDrop")) {
                    updateShapeTypeSpinner();
                }

                if (selectedTrigger.equals("onClick") || selectedTrigger.equals("onEnter") || selectedTrigger.equals("onDrop")) {
                    updateActionSpinner();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        actionListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedAction = actionListSpinner.getSelectedItem().toString();
                updateObjectSpinner(selectedAction);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

    }

    public void updateShapeTypeSpinner() {
        triggerListSpinner = (Spinner) findViewById(R.id.triggerListSpinner);
        shapeTypeListSpinner = (Spinner) findViewById(R.id.shapeTypeListSpinner);

        List<String> currentShapeList = getCurrentShapeNames();

        ArrayAdapter<String> shapeTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currentShapeList);

        shapeTypeListSpinner.setAdapter(shapeTypeAdapter);
    }

    public void updateActionSpinner() {
        triggerListSpinner = (Spinner) findViewById(R.id.triggerListSpinner);
        actionListSpinner = (Spinner) findViewById(R.id.actionListSpinner);

        String[] newActionList = {"action", "goto", "play", "hide", "show"};
        ArrayAdapter<String> actionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, newActionList);

        actionListSpinner.setAdapter(actionAdapter);
    }


    private List<String> getCurrentShapeNames() {
        ArrayList<String> shapeNames = new ArrayList<>();
        for (Page page : game.getPageList()){
            for (Shape shape : page.getShapes()) {
                shapeNames.add(shape.getName());
            }
        }
        return shapeNames;
    }

    public void updateObjectSpinner(String action) {
        actionListSpinner = (Spinner) findViewById(R.id.actionListSpinner);
        objectListSpinner = (Spinner) findViewById(R.id.objectListSpinner);

        ArrayList<String> initObjectList = new ArrayList<>();
        initObjectList.add("object");
        ArrayAdapter<String> initObjectAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, initObjectList);

        ArrayList<String> currentPageList = new ArrayList<>();
        currentPageList.add("page");
        for (int i = 0; i < game.getPageAmount(); i++) {
            currentPageList.add(game.findPageByIndex(i).getName());
        }
        ArrayAdapter<String> pageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currentPageList);

        String[] currentSoundList = {"sound", "carrotcarrotcarrot", "evillaugh", "fire", "hooray", "munch", "munching", "woof"};
        ArrayAdapter<String> soundAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currentSoundList);

        List<String> currentShapeNames = getCurrentShapeNames();
        currentShapeNames.add("shape");
        ArrayAdapter<String> shapeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currentShapeNames);

        if (action.equals("action")) {
            objectListSpinner.setAdapter(initObjectAdapter);
        }
        if (action.equals("goto")) {
            objectListSpinner.setAdapter(pageAdapter);
        }
        if (action.equals("play")) {
            objectListSpinner.setAdapter(soundAdapter);
        }
        if (action.equals("hide") || action.equals("show")) {
            objectListSpinner.setAdapter(shapeAdapter);
        }
    }


    // corresponding page edit button
    public void onAddPage(View view) {
        try {
            String newPageName = "page" + Integer.toString(pageAddCount+1);
            pageAddCount++;

            //check if there is the same name page
            if (game.findPageByName(newPageName) != null) {
                Toast toast = Toast.makeText(getApplicationContext(), "same name existed", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            Page page = new Page(newPageName);
            game.addPage(page);
            currentPage = page;

            pageView = (PageView) findViewById(R.id.editPageView);
            pageView.drawPage(page);
            pageView.importGame(game);

            EditText pageNameET = (EditText) findViewById(R.id.pageNameInput);
            pageNameET.setText(newPageName);

            Toast toast = Toast.makeText(getApplicationContext(), newPageName + " added", Toast.LENGTH_SHORT);
            toast.show();

            // spinner related part
            pageListSpinner = (Spinner) findViewById(R.id.pageNameSpinner);
            ArrayList<String> currentPageList = new ArrayList<>();
            for (int i = 0; i < game.getPageAmount(); i++) {
                currentPageList.add(game.findPageByIndex(i).getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currentPageList);
            pageListSpinner.setAdapter(adapter);

            int newPosition = adapter.getPosition(newPageName);
            pageListSpinner.setSelection(newPosition);

            // update page part script spinner
            actionListSpinner = (Spinner) findViewById(R.id.actionListSpinner);
            objectListSpinner = (Spinner) findViewById(R.id.objectListSpinner);

            String action = actionListSpinner.getSelectedItem().toString();
            if (action.equals("goto")) {
                updateObjectSpinner(action);
            }
        }
        catch(Exception e) {e.printStackTrace();}
    }

    public void onDeletePage(View view) {
        try{

            pageListSpinner = (Spinner) findViewById(R.id.pageNameSpinner);
            EditText pageNameET = (EditText) findViewById(R.id.pageNameInput);

            String pageName = pageListSpinner.getSelectedItem().toString();
            Page page = game.findPageByName(pageName);

            if (page.equals(game.getStartPage())) {
                Toast toast = Toast.makeText(getApplicationContext(), "cannot delete start page", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (page != null) {
//                Canvas canvas = new Canvas();
                System.out.println("Found " + page.getName());
                int pageCount = game.getRemovePageIndex(page);
                game.removePage(page);
                System.out.println("Removed " + page.getName() + " , game has " + game.getPageAmount() + " pages");

                if (pageCount > 0) {
                    currentPage = game.getPageList().get(pageCount-1);
                    pageNameET.setText(currentPage.getName());

                    Toast toast = Toast.makeText(getApplicationContext(), pageName+" deleted", Toast.LENGTH_SHORT);
                    toast.show();
                }
                if (pageCount == 0 & game.getPageAmount() != 0) {
                    currentPage = game.getPageList().get(0);
                    pageNameET.setText(currentPage.getName());

                    Toast toast = Toast.makeText(getApplicationContext(), pageName+" deleted", Toast.LENGTH_SHORT);
                    toast.show();
                }
                if (pageCount == 0 & game.getPageAmount() == 0) {
                    currentPage = new Page("page1");
                    game.addPage(currentPage);
                    pageNameET.setText(currentPage.getName());

                    Toast toast = Toast.makeText(getApplicationContext(), pageName+" deleted", Toast.LENGTH_SHORT);
                    toast.show();
                }
                if (pageCount == -1) {
                    Toast toast = Toast.makeText(getApplicationContext(), "input page not found", Toast.LENGTH_SHORT);
                    toast.show();
                }

                pageView = (PageView) findViewById(R.id.editPageView);
                pageView.drawPage(currentPage);

                // spinner related part
                pageListSpinner = (Spinner) findViewById(R.id.pageNameSpinner);
                ArrayList<String> currentPageList = new ArrayList<>();
                for (int i = 0; i < game.getPageAmount(); i++) {
                    currentPageList.add(game.findPageByIndex(i).getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currentPageList);
                pageListSpinner.setAdapter(adapter);

                int newPosition = adapter.getPosition(currentPage.getName());
                pageListSpinner.setSelection(newPosition);

                // update page part script spinner
                actionListSpinner = (Spinner) findViewById(R.id.actionListSpinner);
                objectListSpinner = (Spinner) findViewById(R.id.objectListSpinner);

                String action = actionListSpinner.getSelectedItem().toString();
                if (action.equals("goto")) {
                    updateObjectSpinner(action);
                }
            }
        }
        catch(Exception e){e.printStackTrace();}
    }


    public void onPageNameChange(View view) {
        try{

            EditText pageNameET = (EditText) findViewById(R.id.pageNameInput);
            String newPageName = pageNameET.getText().toString();

            //check if there is the same name page
            if (game.findPageByName(newPageName) != null) {
                Toast toast = Toast.makeText(getApplicationContext(), "same name existed", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            // cannot have space in name
            if (newPageName.indexOf(" ") != -1) {
                Toast toast = Toast.makeText(getApplicationContext(), "space in name", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            // page cannot be named as "page"
            if (newPageName.equals("page")) {
                Toast toast = Toast.makeText(getApplicationContext(), "not a good name", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }


            game.removePage(currentPage);

            currentPage.setName(newPageName);
            game.addPage(currentPage);
            pageNameET.setText(newPageName);

            pageView = (PageView) findViewById(R.id.editPageView);
            pageView.drawPage(currentPage);


            Toast toast = Toast.makeText(getApplicationContext(), "page name changed", Toast.LENGTH_SHORT);
            toast.show();

            // spinner related part
            pageListSpinner = (Spinner) findViewById(R.id.pageNameSpinner);
            ArrayList<String> currentPageList = new ArrayList<>();
            for (int i = 0; i < game.getPageAmount(); i++) {
                currentPageList.add(game.findPageByIndex(i).getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currentPageList);
            pageListSpinner.setAdapter(adapter);

            int newPosition = adapter.getPosition(newPageName);
            pageListSpinner.setSelection(newPosition);

            // change start page display
            Page page = game.getStartPage();
            TextView startPageET = (TextView) findViewById(R.id.starPageNameTV);
            startPageET.setText(page.getName());
            startPageET.setTextColor(Color.BLUE);

            // update page part script spinner
            actionListSpinner = (Spinner) findViewById(R.id.actionListSpinner);
            objectListSpinner = (Spinner) findViewById(R.id.objectListSpinner);

            String action = actionListSpinner.getSelectedItem().toString();
            if (action.equals("goto")) {
                updateObjectSpinner(action);
            }
        }
        catch(Exception e){e.printStackTrace();}
    }

    public void onGameNameChange(View view) {
        try {
            EditText gameNameET = (EditText) findViewById(R.id.gameNameInput);
            String preGameName = game.getGameName();
            String newGameName = gameNameET.getText().toString();

            if (newGameName.equals("BunnyWorld")) {
                Toast toast = Toast.makeText(getApplicationContext(), "not a good name", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (db.getGameNames().contains(newGameName)) {
                Toast toast = Toast.makeText(getApplicationContext(), "same name existed", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }


            game.setName(newGameName);

            Toast toast = Toast.makeText(getApplicationContext(), "game name changed", Toast.LENGTH_SHORT);
            toast.show();

            //database related
            db.updateGameName(preGameName, newGameName);
        }
        catch(Exception e) {e.printStackTrace();}
    }

    public void onSetStart(View view) {
        try {
            pageListSpinner = (Spinner) findViewById(R.id.pageNameSpinner);
            String pageName = pageListSpinner.getSelectedItem().toString();

            Page page = game.findPageByName(pageName);
            currentPage = page;

            pageView = (PageView) findViewById(R.id.editPageView);
            pageView.drawPage(currentPage);

            EditText pageNameET = (EditText) findViewById(R.id.pageNameInput);
            pageNameET.setText(pageName);

            game.setStartPage(page);

            // change start page display
            currentPage = game.getStartPage();
            TextView startPageET = (TextView) findViewById(R.id.starPageNameTV);
            startPageET.setText(currentPage.getName());
            startPageET.setTextColor(Color.BLUE);

            Toast toast = Toast.makeText(getApplicationContext(), "start page set", Toast.LENGTH_SHORT);
            toast.show();
        }
        catch(Exception e) {e.printStackTrace();}
    }

    public void onAddShape(View view) {
        try {
            shapeListSpinner = (Spinner) findViewById(R.id.shapeFigureSpinner);
            String selectShape = shapeListSpinner.getSelectedItem().toString();
            String str0 = selectShape.substring(selectShape.indexOf(","), selectShape.indexOf("}"));
            String shapeType = str0.substring(str0.indexOf("=") + 1);

            if (shapeType.equals("SelectShape")){
                Toast toast = Toast.makeText(getApplicationContext(), "shape not selected", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            game.removePage(currentPage);

            pageView = (PageView) findViewById(R.id.editPageView);
            currentPage = pageView.getPage();

            for (Shape shape:currentPage.getShapes()) {
                shape.unselect();
            }

            int shapeAmount = 0;
            for (Page page:game.getPageList()) {
                shapeAmount = shapeAmount + page.getShapeAmount();
            }
            shapeAmount = shapeAmount + currentPage.getShapeAmount();
            shapeAmount = shapeAmount + game.getPossession().size();

            String shapeName = "shape" + Integer.toString(shapeAddCount + 1);
            shapeAddCount++;

            //check if there is the same name shape
            if (currentPage.findShapeByName(shapeName) != null) {
                Toast toast = Toast.makeText(getApplicationContext(), "same name existed", Toast.LENGTH_SHORT);
                toast.show();
                game.addPage(currentPage);
                return;
            }

            for (Page page:game.getPageList()) {
                if (page.findShapeByName(shapeName) != null) {
                    Toast toast = Toast.makeText(getApplicationContext(), "same name existed", Toast.LENGTH_SHORT);
                    toast.show();
                    game.addPage(currentPage);
                    return;
                }
            }

            for (Shape shape:game.getPossession()) {
                if (shape.getName().equals(shapeName)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "same name existed", Toast.LENGTH_SHORT);
                    toast.show();
                    game.addPage(currentPage);
                    return;
                }
            }

            String shapeString;
            Bitmap bitmapName;
            if (shapeType.equals("text")) {
                shapeString  = "Text here";
                bitmapName = null;
            }
            else {
                shapeString = "";
                if (shapeType.equals("rect")){
                    bitmapName = null;
                }
                else {
                    bitmapName = ((BitmapDrawable) getResources().getDrawable(drawableMap.get(shapeType))).getBitmap();
                }
            }

            Shape shape = new Shape(shapeName, shapeString, 200, 300, 150, 150, true, bitmapName);
            currentPage.onAdd(shape);

            Toast toast = Toast.makeText(getApplicationContext(), shapeType + " added", Toast.LENGTH_SHORT);
            toast.show();

            pageView = (PageView) findViewById(R.id.editPageView);
            pageView.drawPage(currentPage);
            game.addPage(currentPage);

            EditText shapeNameET = (EditText) findViewById(R.id.shapeNameInput);
            shapeNameET.setText(shapeName);

            TextView xPosET = (TextView) findViewById(R.id.xPositionInput);
            TextView yPosET = (TextView) findViewById(R.id.yPositionInput);
            TextView widthET = (TextView) findViewById(R.id.widthInput);
            TextView heightET = (TextView) findViewById(R.id.heightInput);

            xPosET.setText(new DecimalFormat("0").format(shape.getX()));
            yPosET.setText(new DecimalFormat("0").format(shape.getY()));
            widthET.setText(new DecimalFormat("0").format(shape.getWidth()));
            heightET.setText(new DecimalFormat("0").format(shape.getHeight()));

            // update shape part script spinner
            actionListSpinner = (Spinner) findViewById(R.id.actionListSpinner);
            objectListSpinner = (Spinner) findViewById(R.id.objectListSpinner);

            String action = actionListSpinner.getSelectedItem().toString();
            if (action.equals("hide") ||action.equals("show")) {
                updateObjectSpinner(action);
            }
        }
        catch(Exception e) {e.printStackTrace();}

    }

    private void initDrawableMap() {
        drawableMap.put("carrot", R.drawable.carrot);
        drawableMap.put("carrot2", R.drawable.carrot2);
        drawableMap.put("death", R.drawable.death);
        drawableMap.put("duck", R.drawable.duck);
        drawableMap.put("fire", R.drawable.fire);
        drawableMap.put("mystic", R.drawable.mystic);
    }

    public void onDeleteShape(View view) {
        try {

            pageView = (PageView) findViewById(R.id.editPageView);
            currentShape = pageView.getSelectedShape();

            // check whether a shape is selected
            if (currentShape == null) {
                Toast toast = Toast.makeText(getApplicationContext(), "shape not selected", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            game.removePage(currentPage);

            currentPage.onRemove(currentShape);
            currentShape = null;

            pageView = (PageView) findViewById(R.id.editPageView);
            pageView.drawPage(currentPage);
            game.addPage(currentPage);

            EditText shapeNameET = (EditText) findViewById((R.id.shapeNameInput));
            shapeNameET.setText("");

            Toast toast = Toast.makeText(getApplicationContext(), "shape " + currentShape.getName() + " deleted", Toast.LENGTH_SHORT);
            toast.show();

            // update shape part script spinner
            actionListSpinner = (Spinner) findViewById(R.id.actionListSpinner);
            objectListSpinner = (Spinner) findViewById(R.id.objectListSpinner);

            String action = actionListSpinner.getSelectedItem().toString();
            if (action.equals("hide") || action.equals("show")) {
                updateObjectSpinner(action);
            }
        }
        catch(Exception e){e.printStackTrace();}
    }

    public void onShapeNameChange(View view) {
        try {

            pageView = (PageView) findViewById(R.id.editPageView);
            currentShape = pageView.getSelectedShape();

            // check whether a shape is selected
            if (currentShape == null) {
                Toast toast = Toast.makeText(getApplicationContext(), "shape not selected", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            game.removePage(currentPage);

            String currentShapeName = currentShape.getName();

            EditText shapeNameET = (EditText) findViewById(R.id.shapeNameInput);
            String newShapeName = shapeNameET.getText().toString();

            // shape name cannot have space
            if (currentShapeName.indexOf(" ") != -1) {
                Toast toast = Toast.makeText(getApplicationContext(), "space in name", Toast.LENGTH_SHORT);
                toast.show();
                game.addPage(currentPage);
                return;
            }

            // shape cannot be named as "shape"
            if (currentShapeName.equals("shape")) {
                Toast toast = Toast.makeText(getApplicationContext(), "not a good name", Toast.LENGTH_SHORT);
                toast.show();
                game.addPage(currentPage);
                return;
            }

            //check if there is the same name shape
            if (currentPage.findShapeByName(newShapeName) != null) {
                Toast toast = Toast.makeText(getApplicationContext(), "same name existed", Toast.LENGTH_SHORT);
                toast.show();
                game.addPage(currentPage);
                return;
            }

            for (Page page:game.getPageList()) {
                if (page.findShapeByName(newShapeName) != null) {
                    Toast toast = Toast.makeText(getApplicationContext(), "same name existed", Toast.LENGTH_SHORT);
                    toast.show();
                    game.addPage(currentPage);
                    return;
                }
            }

            for (Shape shape:game.getPossession()) {
                if (shape.getName().equals(newShapeName)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "same name existed", Toast.LENGTH_SHORT);
                    toast.show();
                    game.addPage(currentPage);
                    return;
                }
            }

            currentPage.findShapeByName(currentShapeName).setName(newShapeName);

            pageView = (PageView) findViewById(R.id.editPageView);
            pageView.drawPage(currentPage);
            game.addPage(currentPage);

            Toast toast = Toast.makeText(getApplicationContext(), "shape name changed", Toast.LENGTH_SHORT);
            toast.show();

            // update shape part script spinner
            actionListSpinner = (Spinner) findViewById(R.id.actionListSpinner);
            objectListSpinner = (Spinner) findViewById(R.id.objectListSpinner);

            String action = actionListSpinner.getSelectedItem().toString();
            if (action.equals("hide") || action.equals("show")) {
                updateObjectSpinner(action);
            }
        }
        catch(Exception e){e.printStackTrace();}
    }


    public void onAddScript(View view) {
        try {
            pageView = (PageView) findViewById(R.id.editPageView);
            currentShape = pageView.getSelectedShape();

            if (currentShape == null) {
                Toast toast = Toast.makeText(getApplicationContext(), "shape not selected", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            game.removePage(currentPage);
            triggerListSpinner = (Spinner) findViewById(R.id.triggerListSpinner);
            shapeTypeListSpinner = (Spinner) findViewById(R.id.shapeTypeListSpinner);
            actionListSpinner = (Spinner) findViewById(R.id.actionListSpinner);
            objectListSpinner = (Spinner) findViewById(R.id.objectListSpinner);

            String trigger = triggerListSpinner.getSelectedItem().toString();
            String shapeType = shapeTypeListSpinner.getSelectedItem().toString();
            String action = actionListSpinner.getSelectedItem().toString();
            String object = objectListSpinner.getSelectedItem().toString();

            if (trigger.equals("trigger") || trigger.equals("none")) {
                trigger = "";
            }
            if (shapeType.equals("shape")) {
                shapeType = "";
            }
            if (action.equals("action")) {
                action = "";
            }
            if (object.equals("object") || object.equals("sound") || object.equals("page") || object.equals("shape")) {
                object = "";
            }

            // check script is right
            if (trigger.equals("")) {
                Toast toast = Toast.makeText(getApplicationContext(), "wrong script", Toast.LENGTH_SHORT);
                toast.show();
                game.addPage(currentPage);
                return;
            }

            if (trigger.equals("onDrop") && shapeType.equals("")) {
                Toast toast = Toast.makeText(getApplicationContext(), "wrong script", Toast.LENGTH_SHORT);
                toast.show();
                game.addPage(currentPage);
                return;
            }

            if (action.equals("")) {
                Toast toast = Toast.makeText(getApplicationContext(), "wrong script", Toast.LENGTH_SHORT);
                toast.show();
                game.addPage(currentPage);
                return;
            }

            if (object.equals("")) {
                Toast toast = Toast.makeText(getApplicationContext(), "wrong script", Toast.LENGTH_SHORT);
                toast.show();
                game.addPage(currentPage);
                return;
            }

            String shapeName = currentShape.getName();
            currentPage.findShapeByName(shapeName).getScript().add(trigger, shapeType, action, object);

            pageView = (PageView) findViewById(R.id.editPageView);
            pageView.drawPage(currentPage);
            game.addPage(currentPage);

            Toast toast = Toast.makeText(getApplicationContext(), "script added", Toast.LENGTH_SHORT);
            toast.show();
        }
        catch(Exception e) {e.printStackTrace();}

    }

    public void onSetMovable(View view) {

        CheckBox movableCheckBox = (CheckBox) findViewById(R.id.movableCheckBox);

        pageView = (PageView) findViewById(R.id.editPageView);
        Shape shape = pageView.getSelectedShape();
        if (shape == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "shape not selected", Toast.LENGTH_SHORT);
            toast.show();

            movableCheckBox.setChecked(true);

            return;
        }
        game.removePage(currentPage);
        String currentShapeName = shape.getName();

        if (!movableCheckBox.isChecked()){
            currentPage.findShapeByName(currentShapeName).setUnmovable();

            pageView = (PageView) findViewById(R.id.editPageView);
            pageView.drawPage(currentPage);
            game.addPage(currentPage);

            Toast toast = Toast.makeText(getApplicationContext(), "shape " + currentShapeName + " is unmovable", Toast.LENGTH_SHORT);
            toast.show();

            movableCheckBox.setChecked(false);
        }
        else {
            currentPage.findShapeByName(currentShapeName).setMovable();

            pageView = (PageView) findViewById(R.id.editPageView);
            pageView.drawPage(currentPage);
            game.addPage(currentPage);

            Toast toast = Toast.makeText(getApplicationContext(), "shape " + currentShapeName + " is movable", Toast.LENGTH_SHORT);
            toast.show();

            movableCheckBox.setChecked(true);
        }
    }

    public void onSetVisible(View view) {

        CheckBox visibleCheckBox = (CheckBox) findViewById(R.id.visibleCheckBox);

        pageView = (PageView) findViewById(R.id.editPageView);
        Shape shape = pageView.getSelectedShape();
        if (shape == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "shape not selected", Toast.LENGTH_SHORT);
            toast.show();

            visibleCheckBox.setChecked(true);

            return;
        }

        game.removePage(currentPage);
        String currentShapeName = shape.getName();

        if (!visibleCheckBox.isChecked()){
            currentPage.findShapeByName(currentShapeName).setInvisible();

            pageView = (PageView) findViewById(R.id.editPageView);
            pageView.drawPage(currentPage);
            game.addPage(currentPage);

            Toast toast = Toast.makeText(getApplicationContext(), "shape " + currentShapeName + " is invisible", Toast.LENGTH_SHORT);
            toast.show();

            visibleCheckBox.setChecked(false);
        }
        else {
            currentPage.findShapeByName(currentShapeName).setVisible();

            pageView = (PageView) findViewById(R.id.editPageView);
            pageView.drawPage(currentPage);
            game.addPage(currentPage);

            Toast toast = Toast.makeText(getApplicationContext(), "shape " + currentShapeName + " is visible", Toast.LENGTH_SHORT);
            toast.show();

            visibleCheckBox.setChecked(true);
        }
    }

    public void listenResizeSwitch() {
        Switch resizeSwitch = (Switch) findViewById(R.id.resizeSwitch);
        resizeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast toast = Toast.makeText(getApplicationContext(), "resize mode on", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "resize mode off", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    public void onSetText(View view) {

        pageView = (PageView) findViewById(R.id.editPageView);
        currentShape = pageView.getSelectedShape();

        if (currentShape == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "shape not selected", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        game.removePage(currentPage);
        currentPage = pageView.getPage();

        // dialog part
        AlertDialog.Builder inputTextDialog = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this, R.layout.input_text_dialog_layout, null);
        inputTextDialog.setTitle("Input text").setView(dialogView).create();

        final EditText inputTextET = (EditText) dialogView.findViewById(R.id.inputTextET);
        final Button inputTextButton = (Button) dialogView.findViewById(R.id.inputTextButton);
        final EditText inputTextSizeET = (EditText) dialogView.findViewById(R.id.sizeET);
        final Spinner inputFontSpinner = (Spinner) dialogView.findViewById(R.id.fontSpinner);
        final CheckBox boldCheckBox = (CheckBox) dialogView.findViewById(R.id.boldCheckBox);
        final CheckBox italicCheckBox = (CheckBox) dialogView.findViewById(R.id.italicCheckBox);
        final View colorView = (View) dialogView.findViewById(R.id.colorView);
        final SeekBar redSeekBar = (SeekBar) dialogView.findViewById(R.id.redSeekBar);
        final SeekBar greenSeekBar = (SeekBar) dialogView.findViewById(R.id.greenSeekBar);
        final SeekBar blueSeekBar = (SeekBar) dialogView.findViewById(R.id.blueSeekBar);

        final AlertDialog dialog = inputTextDialog.show();



        inputTextET.setText(currentShape.getText());
        inputTextSizeET.setText(Integer.toString(currentShape.getFontSize()));
        boldCheckBox.setChecked(currentShape.getIsBold());
        italicCheckBox.setChecked(currentShape.getIsItalic());

        int red = Color.red(currentShape.getTextColor());
        int green = Color.green(currentShape.getTextColor());
        int blue = Color.blue(currentShape.getTextColor());

        redSeekBar.setProgress(red);
        greenSeekBar.setProgress(green);
        blueSeekBar.setProgress(blue);

        // show color in real time
        listenColorSeekBar(redSeekBar, greenSeekBar, blueSeekBar, colorView);

        int fontAdapter = inputFontSpinner.getAdapter().getCount();
        for (int pos = 0; pos < fontAdapter; pos++) {
            if (inputFontSpinner.getItemAtPosition(pos).toString().equals(currentShape.getFontType())) {
                inputFontSpinner.setSelection(pos);
                break;
            }
        }

        inputTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputText = inputTextET.getText().toString();
                currentShape.setText(inputText);

                int inputTextSize = Integer.valueOf(inputTextSizeET.getText().toString());
                String inputFont = inputFontSpinner.getSelectedItem().toString();
                Boolean isBold = boldCheckBox.isChecked();
                Boolean isItalic = italicCheckBox.isChecked();
                int red = redSeekBar.getProgress();
                int green = greenSeekBar.getProgress();
                int blue = blueSeekBar.getProgress();

                currentShape.setTextStyle(inputTextSize, inputFont, isBold, isItalic, Color.rgb(red, green, blue));

                Toast toast = Toast.makeText(getApplicationContext(), "input text set", Toast.LENGTH_SHORT);
                toast.show();

                dialog.dismiss();

                game.addPage(currentPage);
                pageView = (PageView) findViewById(R.id.editPageView);
                pageView.drawPage(currentPage);
            }
        });
    }

    public void listenColorSeekBar(SeekBar redSeekBar, SeekBar greenSeekBar, SeekBar blueSeekBar, View colorView) {
        redSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar,int red, boolean b) {
                int blue = blueSeekBar.getProgress();
                int green = greenSeekBar.getProgress();
                colorView.setBackgroundColor(Color.rgb(red,green,blue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        greenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar,int green, boolean b) {
                int red = redSeekBar.getProgress();
                int blue = blueSeekBar.getProgress();
                colorView.setBackgroundColor(Color.rgb(red,green,blue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        blueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar,int blue, boolean b) {
                int red = redSeekBar.getProgress();
                int green = greenSeekBar.getProgress();
                colorView.setBackgroundColor(Color.rgb(red,green,blue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    public void onShowScript(View view) {
        pageView = (PageView) findViewById(R.id.editPageView);
        currentShape = pageView.getSelectedShape();

        if (currentShape == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "shape not selected", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        game.removePage(currentPage);
        currentPage = pageView.getPage();

        // dialog part
        AlertDialog.Builder inputTextDialog = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this, R.layout.shape_script_layout, null);
        inputTextDialog.setTitle("Script show").setView(dialogView).create();

        final TextView shapeNameET = (TextView) dialogView.findViewById(R.id.shapeNameET);
        final TextView shapeScriptET = (TextView) dialogView.findViewById(R.id.shapeScriptET);
        final Button clearButton = (Button) dialogView.findViewById(R.id.clearButton);
        final Button okButton = (Button) dialogView.findViewById(R.id.okButton);

        final AlertDialog dialog = inputTextDialog.show();

        shapeNameET.setText(currentShape.getName());
        shapeScriptET.setText(currentShape.getScript().toString());

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                game.addPage(currentPage);
                pageView = (PageView) findViewById(R.id.editPageView);
                pageView.drawPage(currentPage);
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentShape.setScript(new Script());

                Toast toast = Toast.makeText(getApplicationContext(), "script cleared", Toast.LENGTH_SHORT);
                toast.show();
                dialog.dismiss();

                game.addPage(currentPage);
                pageView = (PageView) findViewById(R.id.editPageView);
                pageView.drawPage(currentPage);
            }
        });
    }

    // show edit button layout
    public void onSetting(View view) {
        drawerLayout = (DrawerLayout) findViewById(R.id.editDrawerLayout);
        setView = (RelativeLayout) findViewById(R.id.setView);
        drawerLayout.openDrawer(setView);
    }

    public void onSaveGame(View view) {

        db.deleteGameContents(game.getGameName());
        db.saveGame(game);

        Toast toast = Toast.makeText(getApplicationContext(), "game saved", Toast.LENGTH_SHORT);
        toast.show();
    }
}