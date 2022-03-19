package edu.stanford.cs108.bunnyworld;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BunnyDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "BunnyDBtest";
    public static final String POSSESSION_ID = "null";
    public BunnyDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.print("DIDN'T FIND DB");
        //TODO: create game and page table
        Cursor tableCursor = db.rawQuery("SELECT * FROM sqlite_master where type='table' and name='shape'", null);
        System.out.println("shape cursor count: " + tableCursor.getCount());
        if (tableCursor.getCount() == 0) {
            System.out.println("Didn't find table shape");
            String CREATE_SHAPE_TABLE = "CREATE TABLE IF NOT EXISTS shape ("
                    + "name TEXT,"
                    + "page_id INTEGER,"
                    + "game_id INTEGER,"
                    + "text TEXT,"
                    + "x REAL,"
                    + "y REAL,"
                    + "width REAL,"
                    + "height REAL,"
                    + "script TEXT,"
                    + "isMovable INTEGER,"
                    + "isVisible INTEGER,"
                    + "isSelected INTEGER,"
                    + "fontSize INTEGER,"
                    + "textColor INTEGER,"
                    + "rectColor INTEGER,"
                    + "selectColor INTEGER,"
                    + "bitmap BLOB,"
                    + "fontType TEXT,"
                    + "isBold INTEGER,"
                    + "isItalic INTEGER,"
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                    + ");";
            db.execSQL(CREATE_SHAPE_TABLE);
        }

        tableCursor = db.rawQuery("SELECT * FROM sqlite_master where type='table' and name='page';", null);
        System.out.println("page cursor count: " + tableCursor.getCount());
        if (tableCursor.getCount() == 0) {
            System.out.println("Didn't find table page");
            String CREATE_PAGE_TABLE = "CREATE TABLE IF NOT EXISTS page ("
                    + "name TEXT,"
                    + "game_id INTEGER,"
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                    + ");";
            db.execSQL(CREATE_PAGE_TABLE);
        }

        tableCursor = db.rawQuery("SELECT * FROM sqlite_master where type='table' and name='game';", null);
        System.out.println("game cursor count: " + tableCursor.getCount());
        if(tableCursor.getCount() == 0) {
            System.out.println("Didn't find table game");
            String CREATE_GAME_TABLE = "CREATE TABLE IF NOT EXISTS game ("
                    + "name TEXT,"
                    + "starter_page_name TEXT,"
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                    + ");";
            db.execSQL(CREATE_GAME_TABLE);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
    }


    private void deletePossessionByGameId(Integer gameId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("shape", "game_id =? and page_id = ?", new String[]{String.valueOf(gameId), POSSESSION_ID});
    }

    private void addPossessionByGameId(Integer gameId, Game game) {
        for (Shape shape : game.getPossession()) {
            addShape(shape, null, gameId);
        }
    }


    private byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    private Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public void addShape(Shape shape, Integer pageID, Integer gameID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", shape.getName());
        values.put("page_id", pageID);
        values.put("game_id", gameID);
        values.put("text", shape.getText());
        values.put("x", shape.getX());
        values.put("y", shape.getY());
        values.put("width", shape.getWidth());
        values.put("height", shape.getHeight());
        values.put("script", shape.getScript().toString());
        values.put("isMovable", shape.isMovable() ? 1 : 0);
        values.put("isVisible", shape.isVisible() ? 1 : 0);
        values.put("isSelected", shape.isSelected() ? 1 : 0);
        values.put("fontSize", shape.getFontSize());
        values.put("textColor", shape.getTextColor());
        values.put("rectColor", shape.getRectColor());
        values.put("selectColor", shape.getSelectColor());
        values.put("fontType", shape.getFontType());
        values.put("isBold", shape.getIsBold());
        values.put("isItalic", shape.getIsItalic());
        if (shape.getBitmap() == null) {
            values.put("bitmap", new byte[0]);
        } else {
            byte[] image = getBytes(shape.getBitmap());
            values.put("bitmap", image);
        }

        db.insert("shape", null, values);
        //db.close();
    }



    public static final String[] GAME_ID_COLUMN = {"_id"};

    public Integer findGameIdByName(String gameName) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("game", GAME_ID_COLUMN, "name = ?", new String[]{gameName},
                null, null, null, null);

        while (cursor.moveToNext()) {
            return cursor.getInt(0);
        }
        //db.close();
        return null;
    }

    public static final String[] PAGE_GAME_ID_COLUMN = {"game_id"};

    public Integer findGameIdByPage(Integer pageID) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("page", PAGE_GAME_ID_COLUMN, "_id = ?", new String[]{String.valueOf(pageID)},
                null, null, null, null);
        while (cursor.moveToNext()) {
            return cursor.getInt(0);
        }
        //
        // db.close();
        return null;
    }

    public static final String[] PAGE_ID_COLUMN= {"_id"};

    public List<Integer> findPageIdsByGame(Integer gameId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("page", PAGE_ID_COLUMN, "game_id = ?", new String[]{String.valueOf(gameId)},
                null, null, null, null);

        List<Integer> pageIds = new ArrayList<>();
        while (cursor.moveToNext()) {
            pageIds.add(cursor.getInt(0));
        }
        //db.close();
        return pageIds;
    }

    public Integer findPageId(String pageName, Integer gameId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("page", PAGE_ID_COLUMN, "name = ? AND game_id = ?",
                new String[]{pageName, String.valueOf(gameId)}, null, null, null, null);

        while (cursor.moveToNext()) {
            return cursor.getInt(0);
        }
        //db.close();
        return null;
    }

    public static final String[] SHAPE_COLUMNS = {
            "name", "page_id", "game_id", "text", "x", "y", "width", "height",
            "script", "isMovable", "isVisible", "isSelected", "fontSize", "textColor", "rectColor",
            "selectColor", "bitmap", "fontType", "isBold", "isItalic"};

    private Shape constructShape(Cursor cursor) {
        String name = cursor.getString(0);
        String text = cursor.getString(3);
        float x = cursor.getFloat(4);
        float y = cursor.getFloat(5);
        float width = cursor.getFloat(6);
        float height = cursor.getFloat(7);
        String script = cursor.getString(8);
        boolean isMovable = cursor.getInt(9) == 1 ? true : false;
        boolean isVisible = cursor.getInt(10) == 1 ? true : false;
        boolean isSelected = cursor.getInt(11) == 1 ? true : false;
        int fontSize = cursor.getInt(12);
        int textColor = cursor.getInt(13);
        int rectColor = cursor.getInt(14);
        int selectColor = cursor.getInt(15);
        byte[] image = cursor.getBlob(16);
        Bitmap bitmap = (image == null || image.length == 0) ? null : getImage(image);
        String fontType = cursor.getString(17);
        boolean isBold = cursor.getInt(18) == 1 ? true : false;
        boolean isItalic = cursor.getInt(19) == 1 ? true : false;

        Shape shape = new Shape(name, text, x, y, width, height, false, bitmap);
        System.out.println("SHAPE NAME: " + name);
        System.out.println("Script: " + script);
        shape.setScript(Script.fromString(script));
        if (isMovable) shape.setMovable();
        else shape.setUnmovable();

        if (isVisible) shape.setVisible();
        else shape.setInvisible();

//        shape.setFontSize(fontSize);
//        shape.setTextColor(textColor);
        shape.setTextStyle(fontSize, fontType, isBold, isItalic, textColor);
        shape.setRectColor(rectColor);
        shape.setSelectColor(selectColor);
        return shape;
    }

    public List<Shape> getShapesByPage(int pageID) {
        List<Shape> shapes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("shape", SHAPE_COLUMNS, "page_id = ?",
                new String[]{String.valueOf(pageID)}, null, null, null, null);

        while (cursor.moveToNext()) {
            shapes.add(constructShape(cursor));
        }

        //db.close();
        return shapes;
    }

    public static final String[] PAGE_ID_NAME_COLUMN = {"_id, name"};

    public List<Page> getPagesByGameId(int gameID) {
        ArrayList<Page> pages = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query("page", PAGE_ID_NAME_COLUMN, "game_id =? ",
                new String[]{String.valueOf(gameID)}, null, null,
                null, null);

        while(cursor.moveToNext()) {
            Integer pageID = cursor.getInt(0);
            Page page = new Page(cursor.getString(1));

            List<Shape> shapes = getShapesByPage(pageID);

            for (Shape shape : shapes) {
                System.out.println("Found " + shape.getName() + " for " + page.getName());
                page.onAdd(shape);
            }

            pages.add(page);
        }

        System.out.println("DB found " + pages.size() + " Pages");

        return pages;
    }

    public static final String[] GAME_STARTER_PAGE_COLUMN = {"starter_page_name"};
    public String getGameStarterPage(int gameID) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query("game", GAME_STARTER_PAGE_COLUMN, "_id =?",
                new String[]{String.valueOf(gameID)}, null, null, null, null);

        while (cursor.moveToNext()) {
            return cursor.getString(0);
        }
        return null;
    }

    public Game getGameByName(String gameName) {
        Game game = new Game(gameName);

        Page starterPage;

        Integer gameID = findGameIdByName(gameName);
        if (gameID == null) return null;
        String starterPageName = getGameStarterPage(gameID);

        List<Page> pages = getPagesByGameId(gameID);

        System.out.println("getGameByName found " + pages.size() + " pages");

        for (Page page : pages) {
            game.addPage(page);

            if (page.getName().equals(starterPageName)) {
                game.setStartPage(page);
            }
        }

        System.out.println("DB build a game with " + game.getPageAmount() + " pages");

        // TODO: deal with possessions
        List<Shape> possession = getPossessionByGameId(gameID);
        for (Shape shape : possession) {
            game.addShapeToPossession(shape);
        }

        return game;
    }

    public List<Shape> getPossessionByGameId(Integer gameId) {
        List<Shape> possession = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("shape", SHAPE_COLUMNS, "game_id = ? and page_id is null",
                new String[]{String.valueOf(gameId)}, null, null, null, null);

        while (cursor.moveToNext()) {
            possession.add(constructShape(cursor));
        }

        //db.close();
        return possession;
    }

    public void deletePage(Integer pageID) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("page", "_id =? ", new String[]{String.valueOf(pageID)});
        //db.close();
    }

    public void deleteShapesByPage(Integer pageID) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("shape", "page_id =? ", new String[]{String.valueOf(pageID)});
        //db.close();
    }


    public void addShapesByPage(Integer pageID, Page page) {
        Integer gameID = findGameIdByPage(pageID);
        for (Shape shape : page.getShapes()) {
            addShape(shape, pageID, gameID);
        }
    }






    public void deletePagesByGame(Integer gameID) {
        for (Integer pageId: findPageIdsByGame(gameID)) {
            deletePageContentsByPageID(pageId);
        }
    }


    public void deleteGame(Integer gameID) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("game", "_id =? ", new String[]{String.valueOf(gameID)});
    }



    /*

    ---------------------------for frontend use-------------------------------------
     */


    public void updateGameName(String gameName, String newGameName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", newGameName);
        db.update("game", contentValues, "name = ?", new String[]{gameName});
    }

    public void updateGameStartPage(String gameName, String startPageName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("starter_page_name", startPageName);
        db.update("game", contentValues, "name = ?", new String[]{gameName});
    }

    public void updatePossession(Game game) {
        Integer gameID = findGameIdByName(game.getGameName());
        deletePossessionByGameId(gameID);
        addPossessionByGameId(gameID, game);

    }

    //for save page button
    public void updatePage(String gameName, String pageName, Page page) {
        Integer gameID = findGameIdByName(gameName);
        Integer pageID = findPageId(pageName, gameID);
        deleteShapesByPage(pageID);
        addShapesByPage(pageID, page);
    }

    public void updatePageName(String gameName, String pageName,String newPageName) {
        Integer gameID = findGameIdByName(gameName);
        Integer pageID = findPageId(pageName, gameID);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", newPageName);
        db.update("page", contentValues, "_id = ?", new String[]{String.valueOf(pageID)});
    }


    public void deleteGameContents(String gameName) {
        Integer gameID = findGameIdByName(gameName);
        deleteGame(gameID);
        deletePagesByGame(gameID);
    }

    public void deletePageContents(String gameName, String pageName) {
        Integer gameID = findGameIdByName(gameName);
        Integer pageID = findPageId(pageName, gameID);
        deletePage(pageID);
        deleteShapesByPage(pageID);
    }

    public void deletePageContentsByPageID(Integer pageID) {
        deletePage(pageID);
        deleteShapesByPage(pageID);
    }



    public void addGame(Game game) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", game.getGameName());
        values.put("starter_page_name", game.getStartPage().getName());

        db.insert("game", null, values);
        //db.close();

//        // update possession
//        Integer gameID = findGameIdByName(game.getGameName());
//        deletePossessionByGameId(gameID);
//        addPossessionByGameId(gameID, game);

    }

    public void saveGame(Game game) {
        addGame(game);
        Integer gameID = findGameIdByName(game.getGameName());
        for (Shape shape : game.getPossession()) {
            addShape(shape, null, gameID);
        }
        for (Page page : game.getPageList()) {
            addPage(page, game.getGameName());
            Integer pageID = findPageId(page.getName(), gameID);
            addShapesByPage(pageID, page);
        }
    }

    public void addPage(Page page, String gameName) {
        Integer gameID = findGameIdByName(gameName);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", page.getName());
        values.put("game_id", gameID);

        db.insert("page", null, values);

    }

    public List<String> getGameNames() {
        List<String> gameNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("game", new String[]{"name"}, null,
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            gameNames.add(cursor.getString(0));
        }
        return gameNames;
    }

}
