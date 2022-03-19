package edu.stanford.cs108.bunnyworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.os.Bundle;
import android.content.Intent;
import android.widget.*;
import android.database.*;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    //    SQLiteDatabase db;
    BunnyDB db = new BunnyDB(this);

    private Spinner gameSaveSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// app start in landscape view
        initDataBaseSpinner();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        initDataBaseSpinner();
    }

    private void initDefaultGame() {
        Game game = new Game("BunnyWorld");
        Page p1 = new Page("page1");
        Page p2 = new Page("page2");
        Page p3 = new Page("page3");
        Page p4 = new Page("page4");
        Page p5 = new Page("page5");
        game.setStartPage(p1);
        game.addPage(p1);
        game.addPage(p2);
        game.addPage(p3);
        game.addPage(p4);
        game.addPage(p5);
        Shape p1Title = new Shape("p1title", "Bunny World!", 700, 150, 300, 50, true, null);
        Shape p1door1 = new Shape("p1door1", "", 500, 500, 70, 120, true, null);
        Shape p1door2 = new Shape("p1door2", "", 1000, 500, 70, 120, true, null);
        Shape p1door3 = new Shape("p1door3", "", 1500, 500, 70, 120, true, null);
        p1Title.setTextColor(Color.BLUE);
        p1Title.setFontSize(200);
        p1door2.setInvisible();
        p1Title.setUnmovable();
        p1door1.setUnmovable();
        p1door2.setUnmovable();
        p1door3.setUnmovable();
        p1door1.getScript().add("onClick", null, "goto", "page2");
        p1door2.getScript().add("onClick", null, "goto", "page3");
        p1door3.getScript().add("onClick", null, "goto", "page4");
        p1.getShapes().add(p1Title);
        p1.getShapes().add(p1door1);
        p1.getShapes().add(p1door2);
        p1.getShapes().add(p1door3);

        Shape p2rabbit = new Shape("p2rabbit", "", 700, 250, 350, 350, true, ((BitmapDrawable) getResources().getDrawable(R.drawable.mystic)).getBitmap());
        Shape p2door = new Shape("p2door", "", 300, 500, 70, 120, true, null);
        p2rabbit.getScript().add("onEnter", null, "show", "p1door2");
        p2rabbit.getScript().add("onClick", null, "hide", "p3carrot");
        p2rabbit.getScript().add("onClick", null, "play", "carrotcarrotcarrot");
        p2door.getScript().add("onClick", null, "goto", "page1");
        p2.getShapes().add(p2rabbit);
        p2.getShapes().add(p2door);
        p2door.setUnmovable();
        p2rabbit.setUnmovable();

        Shape p3carrot = new Shape("p3carrot", "", 1400, 400, 250, 250, true, ((BitmapDrawable) getResources().getDrawable(R.drawable.carrot)).getBitmap());
        p3carrot.setMovable();
        Shape p3fire = new Shape("p3fire", "", 600, 100, 400, 400, true, ((BitmapDrawable) getResources().getDrawable(R.drawable.fire)).getBitmap());
        p3fire.setUnmovable();
        p3fire.getScript().add("onEnter", null, "play", "fire");
        Shape p3door = new Shape("p3door", "", 300, 550, 70, 120, true, null);
        p3door.setUnmovable();
        p3door.getScript().add("onClick", null, "goto", "page2");
        p3.getShapes().add(p3fire);
        p3.getShapes().add(p3carrot);
        p3.getShapes().add(p3door);

        Shape p4rabbit = new Shape("p4rabbit", "", 800, 200, 300, 300, true, ((BitmapDrawable) getResources().getDrawable(R.drawable.death)).getBitmap());
        p4rabbit.setUnmovable();
        p4rabbit.getScript().add("onEnter", null, "play", "evillaugh");
        p4rabbit.getScript().add("onDrop", "p3carrot", "hide", "p3carrot");
        p4rabbit.getScript().add("onDrop","p3carrot", "play", "carrotcarrotcarrot");
        p4rabbit.getScript().add("onDrop", "p3carrot", "hide", "p4rabbit");
        p4rabbit.getScript().add("onDrop", "p3carrot", "show", "p4door");
        Shape p4door = new Shape("p4door", "", 400, 500, 70, 120, true, null);
        p4door.setUnmovable();
        p4door.setInvisible();
        p4door.getScript().add("onClick", null, "goto", "page5");
        p4.getShapes().add(p4rabbit);
        p4.getShapes().add(p4door);

        Shape p5text = new Shape("p5text", "You Win! Yay!", 500, 600, 500, 100, true, null);
        p5text.setUnmovable();
        p5text.setFontSize(180);
        p5text.setTextColor(Color.RED);
        Shape p5carrot1 = new Shape("p5carrot1", "", 300, 120, 400, 400, true, ((BitmapDrawable) getResources().getDrawable(R.drawable.carrot)).getBitmap());
        Shape p5carrot2 = new Shape("p5carrot2", "", 750, 120, 400, 400, true, ((BitmapDrawable) getResources().getDrawable(R.drawable.carrot)).getBitmap());
        Shape p5carrot3 = new Shape("p5carrot3", "", 1200, 120, 400, 400, true, ((BitmapDrawable) getResources().getDrawable(R.drawable.carrot)).getBitmap());
        p5carrot1.setUnmovable();
        p5carrot2.setUnmovable();
        p5carrot3.setUnmovable();
        p5carrot1.getScript().add("onEnter", null, "play", "hooray");
        p5.getShapes().add(p5carrot1);
        p5.getShapes().add(p5carrot2);
        p5.getShapes().add(p5carrot3);
        p5.getShapes().add(p5text);
        db.saveGame(game);
    }

    public void initDataBaseSpinner() {
        db.deleteGameContents("BunnyWorld");
        initDefaultGame();

        gameSaveSpinner = (Spinner) findViewById(R.id.gameSaveSpinner);
        List<String> gameSaveList = db.getGameNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, gameSaveList);
        gameSaveSpinner.setAdapter(adapter);
    }

    public void onPlayGame(View view) {
        Intent intent = new Intent(this, PlayGameActivity.class);

        gameSaveSpinner = (Spinner) findViewById(R.id.gameSaveSpinner);
        String gameName = gameSaveSpinner.getSelectedItem().toString();

        if(db.getGameByName(gameName) == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "game not exist", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }


        intent.putExtra("gameName", gameName);

        startActivity(intent);

    }

    public void onEditGame(View view) {
        Intent intent = new Intent(this, EditGameActivity.class);

        gameSaveSpinner = (Spinner) findViewById(R.id.gameSaveSpinner);
        String gameName = gameSaveSpinner.getSelectedItem().toString();

        if(db.getGameByName(gameName) == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "game not exist", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        intent.putExtra("gameName", gameName);
        intent.putExtra("newGame", false);

        startActivity(intent);

    }

    public void onDeleteGame(View view) {
        gameSaveSpinner = (Spinner) findViewById(R.id.gameSaveSpinner);
        String gameName = gameSaveSpinner.getSelectedItem().toString();

        if(db.getGameByName(gameName) == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "game not exist", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        db.deleteGameContents(gameName);

        Toast toast = Toast.makeText(getApplicationContext(), "Game Deleted", Toast.LENGTH_SHORT);
        toast.show();

        initDataBaseSpinner();

    }

    public void onNewGame(View view) {
        Intent intent = new Intent(this, EditGameActivity.class);

        String gameName = "NewGame" + Integer.toString(db.getGameNames().size());
        if (db.getGameByName(gameName) != null) {
            Toast toast = Toast.makeText(getApplicationContext(), "same name exist", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        intent.putExtra("gameName", gameName);
        intent.putExtra("newGame", true);

        Toast toast = Toast.makeText(getApplicationContext(), "New Game Created", Toast.LENGTH_SHORT);
        toast.show();
        startActivity(intent);

    }

}