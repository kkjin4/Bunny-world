package edu.stanford.cs108.bunnyworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PlayGameActivity extends AppCompatActivity {

    BunnyDB db = new BunnyDB(this);

    private PageView pageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// app start in landscape view

        String gameName = getIntent().getStringExtra("gameName");
        Game game = db.getGameByName(gameName);

        if (game == null) {
            game = new Game("NewGme");
            Page currentPage = new Page("page1");
            game.addPage(currentPage);
            game.setStartPage(currentPage);

            Toast toast = Toast.makeText(getApplicationContext(), "game not found", Toast.LENGTH_SHORT);
            toast.show();
        }

        Page currentPage = game.getStartPage();
        pageView = (PageView) findViewById(R.id.playPageView);
        pageView.importGame(game);
        pageView.drawPage(currentPage);
        pageView.setActive(true);

        PageView pageView = findViewById(R.id.playPageView);
        pageView.init(game);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        View separateLine = (View) findViewById(R.id.separateLine);
        int[] possessionXY =  new int[2];
        separateLine.getLocationOnScreen(possessionXY);
        int possessionY = possessionXY[1];

        pageView = (PageView) findViewById(R.id.playPageView);
        possessionY = separateLine.getTop();
        pageView.setPossessionY(possessionY);
    }
}