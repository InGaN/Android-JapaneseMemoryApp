package com.example.kevin.japanesememoryapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    FeedReaderDbHelper dbHelper;
    ArrayList<Kanji> kanjiList;
    int[] kanjiArray;
    TextView lbl_kanji;
    TextView lbl_furigana;
    TextView lbl_meaning;
    TextView lbl_difficulty;
    TextView lbl_paused;
    RelativeLayout con_buttons;

    Handler timerHandler;
    Runnable timerRunnable;
    int timerIndex = 0;
    int timerMaxReveal, timerMaxNext;
    boolean timerRevealing = true;
    boolean timerPaused = true;

    boolean showFurigana, showKanji, showMeaning, showDifficulty;
    int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = getSharedPreferences(SettingsActivity.PREFERENCES_FILE_NAME, 0);
        applyTheme(settings);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        RelativeLayout main = (RelativeLayout)findViewById(R.id.con_main);
        lbl_furigana = (TextView)findViewById(R.id.lbl_furigana);
        lbl_kanji = (TextView)findViewById(R.id.lbl_kanji);
        lbl_meaning = (TextView)findViewById(R.id.lbl_meaning);
        lbl_difficulty = (TextView)findViewById(R.id.lbl_difficulty);
        lbl_paused = (TextView)findViewById(R.id.lbl_paused);
        con_buttons = (RelativeLayout)findViewById(R.id.con_buttons);

        OnSwipeTouchListener swiperListener = new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeTop() {
                Toast.makeText(MainActivity.this, "top", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeRight() {
                Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();
                currentIndex = (currentIndex + 1) % kanjiList.size();
                getKanji(currentIndex);
            }

            public void onSwipeLeft() {
                Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT).show();
                currentIndex = (currentIndex - 1 < 0) ? kanjiList.size() - 1 : currentIndex - 1;
                getKanji(currentIndex);
            }

            public void onSwipeBottom() {
                Toast.makeText(MainActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }
        };

        main.setOnTouchListener(swiperListener);

        lbl_paused.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerPaused = !timerPaused;
                Toast.makeText(MainActivity.this, (timerPaused) ? getString(R.string.paused) : getString(R.string.resume), Toast.LENGTH_SHORT).show();
            }
        });

        hideAnswer();
        initializeViews(settings);

        Button btn_toInput = (Button)findViewById(R.id.btn_toInput);
        btn_toInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callInputActivity();
            }
        });
        Button btn_toList = (Button)findViewById(R.id.btn_toList);
        btn_toList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callKanjiList();
            }
        });
        Button btn_toSettings = (Button)findViewById(R.id.btn_toSettings);
        btn_toSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSettings();
            }
        });

        initializeKanji();
    }

    private void applyTheme(SharedPreferences settings) {
        switch((int)settings.getLong("themeSelection", 0L)) {
            case 0:
                setTheme(R.style.AppThemeLight);
                break;
            case 1:
                setTheme(R.style.AppThemeDark);
                break;
            default:
                setTheme(R.style.AppThemeDark);
                break;
        }
    }

    @Override
    public void onResume() {
        SharedPreferences settings = getSharedPreferences(SettingsActivity.PREFERENCES_FILE_NAME, 0);
        applyTheme(settings);
        super.onResume();
        initializeKanji();
        hideAnswer();
        initializeViews(settings);
    }

    private void initializeViews(SharedPreferences settings) {
        showFurigana = settings.getBoolean("furiganaActive", true);
        showKanji = settings.getBoolean("kanjiActive", true);
        showMeaning = settings.getBoolean("meaningActive", true);
        showDifficulty = settings.getBoolean("difficultyActive", true);
        con_buttons.setVisibility(settings.getBoolean("showMenuButtons", true) ? View.VISIBLE : View.INVISIBLE);

        setQuestionMode(settings);
    }

    private void initializeKanji() {
        kanjiList = getKanjiFromDatabase();
        Log.d("array", "array: " + Arrays.toString(kanjiArray));
        if(kanjiArray.length > 0)
            getKanji(kanjiArray[currentIndex]);
        else {
            getKanji(0);

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            callInputActivity();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            //Do nothing, close dialog
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(getString(R.string.mainWelcomeMessage)).setPositiveButton(getString(R.string.addNew), dialogClickListener).setNegativeButton(getString(R.string.cancel), dialogClickListener).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.action_settings:
                callSettings();
                return true;
            case R.id.action_kanjilist:
                callKanjiList();
                return true;
            case R.id.action_input:
                callInputActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private ArrayList<Kanji> getKanjiFromDatabase() {
        dbHelper = new FeedReaderDbHelper(MainActivity.this);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectQuery = {
                FeedReaderContract.FeedEntry.COLUMN_NAME_ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_KANJI,
                FeedReaderContract.FeedEntry.COLUMN_NAME_FURIGANA,
                FeedReaderContract.FeedEntry.COLUMN_NAME_MEANING,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DIFFICULTY
        };

        String sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_ID + " ASC";

        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                selectQuery,
                null,
                null,
                null,
                null,
                sortOrder
        );

        if (cursor != null) {
            ArrayList<Kanji> kanji = new ArrayList<>();
            cursor.moveToFirst();

            for(int x = 0; x < cursor.getCount(); x++) {
                kanji.add( new Kanji(
                        Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        Byte.parseByte(cursor.getString(4))
                ));
                cursor.moveToNext();
            }
            kanjiArray = new int[kanji.size()];
            for(int x = 0; x < kanji.size(); x++) {
                kanjiArray[x] = x;
            }
            FisherYatesShuffleArray(kanjiArray);
            return kanji;
        }
        return null;
    }

    private void getKanji(int index) {
        if(kanjiList != null) {
            if (kanjiList.size() > 0) {
                Kanji currentKanji = kanjiList.get(index);

                lbl_kanji.setText(currentKanji.getKanji());
                lbl_furigana.setText(currentKanji.getFurigana());
                lbl_meaning.setText(currentKanji.getMeaning());
                lbl_difficulty.setText(currentKanji.getDifficulty() + "/9");
                hideAnswer();
            }
            else {
                lbl_kanji.setText("");
                lbl_furigana.setText("");
                lbl_meaning.setText("Kanji list is empty...");
                lbl_difficulty.setText("");
            }
        }
        else {
            lbl_kanji.setText("");
            lbl_furigana.setText("");
            lbl_meaning.setText("Unable to load Kanji...");
            lbl_difficulty.setText("");
        }
    }

    private void showAnswer() {
        lbl_furigana.setVisibility(View.VISIBLE);
        lbl_kanji.setVisibility(View.VISIBLE);
        lbl_meaning.setVisibility(View.VISIBLE);
    }
    private void hideAnswer() {
        lbl_furigana.setVisibility((showFurigana) ? View.VISIBLE : View.INVISIBLE);
        lbl_kanji.setVisibility((showKanji) ? View.VISIBLE : View.INVISIBLE);
        lbl_meaning.setVisibility((showMeaning) ? View.VISIBLE : View.INVISIBLE);
        lbl_difficulty.setVisibility((showDifficulty) ? View.VISIBLE : View.INVISIBLE);
    }

    private void setQuestionMode(SharedPreferences settings) {
        long mode = settings.getLong("questionMode", 0L);

        final ProgressBar bar_timer = (ProgressBar)findViewById(R.id.bar_timer);
        bar_timer.setVisibility(mode == 0 ? View.VISIBLE : View.INVISIBLE);
        lbl_paused.setVisibility(mode == 0 ? View.VISIBLE : View.INVISIBLE);

        if(mode == 0) {
            timerMaxReveal = settings.getInt("secondsToReveal", 60);
            timerMaxNext = settings.getInt("secondsToNext", 10);
            bar_timer.setMax(timerMaxReveal);
            timerHandler = new Handler();
            timerRunnable = new Runnable() {
                @Override
                public void run() {
                    if(!timerPaused) {
                        lbl_paused.setVisibility(View.INVISIBLE);
                        bar_timer.setProgress(timerIndex);
                        timerIndex++;
                        timerHandler.postDelayed(timerRunnable, 1000);
                        if (timerRevealing) {
                            if (timerIndex > timerMaxReveal + 1) {
                                timerRevealing = false;
                                bar_timer.setMax(timerMaxNext);
                                bar_timer.setProgress(0);
                                timerIndex = 0;
                                showAnswer();
                            }
                        }
                        else {
                            if (timerIndex > timerMaxNext + 1) {
                                timerRevealing = true;
                                bar_timer.setMax(timerMaxReveal);
                                bar_timer.setProgress(0);
                                timerIndex = 0;
                                hideAnswer();
                                currentIndex = (currentIndex + 1) % kanjiList.size();
                                getKanji(currentIndex);
                            }
                        }
                    }
                    else {
                        lbl_paused.setVisibility(View.VISIBLE);
                        timerHandler.postDelayed(timerRunnable, 1000);
                    }
                }
            };
            timerHandler.postDelayed(timerRunnable, 1000);
        }
        else if(mode == 1) {
            lbl_kanji.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAnswer();
                }
            });
        }
    }

    public static void showAlert(Context context, String title, String message) {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void callInputActivity() {
        timerPaused = true;
        if(timerHandler != null)
            timerHandler.removeCallbacks(timerRunnable);
        Intent intent = new Intent(this, InputActivity.class);
        startActivity(intent);
    }

    private void callKanjiList() {
        timerPaused = true;
        if(timerHandler != null)
            timerHandler.removeCallbacks(timerRunnable);
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    private void callSettings() {
        timerPaused = true;
        if(timerHandler != null)
            timerHandler.removeCallbacks(timerRunnable);
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    static void FisherYatesShuffleArray(int[] array)
    {
        int n = array.length;
        for (int i = 0; i < array.length; i++) {
            int random = i + (int) (Math.random() * (n - i));
            int randomElement = array[random];
            array[random] = array[i];
            array[i] = randomElement;
        }
    }
}
