package com.myKanji;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static int SIZE_FURIGANA = 30;
    private static int SIZE_KANJI = 45;
    private static int SIZE_MEANING = 20;
    private static int SIZE_DIFFICULTY = 10;
    public static String PACKAGE_NAME;

    ArrayList<Kanji> kanjiList;
    int[] kanjiArray;
    TextView lbl_kanji;
    TextView lbl_furigana;
    TextView lbl_meaning;
    TextView lbl_difficulty;
    TextView lbl_paused;
    TextView lbl_errors;
    RelativeLayout con_menuButtons;
    ProgressBar bar_timer;
    EditText tbx_input;
    RelativeLayout con_yesNoButtons;
    Button btn_no;
    Button btn_yes;
    Button btn_reveal;

    private Handler timerHandler;
    private Runnable timerRunnable;

    int timerIndex = 0;
    int timerMaxReveal, timerMaxNext;
    boolean timerPaused = true;
    boolean revealed = false;
    boolean swipeMode = false;
    boolean inputMode = false;
    boolean errorActive = false;
    boolean editSizes = false;
    long inputModeType;

    Kanji currentKanji;

    boolean showFurigana, showKanji, showMeaning, showDifficulty;
    int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = getSharedPreferences(SettingsActivity.PREFERENCES_FILE_NAME, 0);
        applyTheme(settings);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PACKAGE_NAME = getApplicationContext().getPackageName();

        RelativeLayout main = (RelativeLayout)findViewById(R.id.con_main);
        lbl_furigana = (TextView)findViewById(R.id.lbl_furigana);
        lbl_kanji = (TextView)findViewById(R.id.lbl_kanji);
        lbl_meaning = (TextView)findViewById(R.id.lbl_meaning);
        lbl_difficulty = (TextView)findViewById(R.id.lbl_difficulty);
        lbl_paused = (TextView)findViewById(R.id.lbl_paused);
        lbl_errors = (TextView)findViewById(R.id.lbl_errors);
        lbl_errors.setVisibility(View.GONE);
        con_menuButtons = (RelativeLayout)findViewById(R.id.con_menuButtons);
        bar_timer = (ProgressBar)findViewById(R.id.bar_timer);
        tbx_input = (EditText)findViewById(R.id.tbx_input);
        tbx_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN ))) {
                    showAnswer();
                    btn_reveal.setText(getString(R.string.next));
                    revealed = true;
                    return true;
                }
                else {
                    return false;
                }
            }
        });

        con_yesNoButtons = (RelativeLayout)findViewById(R.id.con_yesNoButtons);
        btn_no = (Button)findViewById(R.id.btn_no);
        btn_yes = (Button)findViewById(R.id.btn_yes);
        btn_reveal = (Button)findViewById(R.id.btn_reveal);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alterDifficulty(1, currentKanji);
                btn_no.setEnabled(false);
                btn_yes.setEnabled(false);
            }
        });
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alterDifficulty(-1, currentKanji);
                btn_no.setEnabled(false);
                btn_yes.setEnabled(false);
            }
        });
        btn_reveal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!revealed) {
                    showAnswer();
                    btn_reveal.setText(getString(R.string.next));
                } else {
                    hideAnswer();
                    currentIndex = (currentIndex + 1) % kanjiList.size();
                    fillLabelsWithKanji();
                    btn_reveal.setText(getString(R.string.reveal));
                }
            }
        });

        OnSwipeTouchListener swiperListener = new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeTop() {
                if(swipeMode) {
                    showAnswer();
                }
            }
            public void onSwipeRight() {
                if(swipeMode) {
                    if(revealed) {
                        currentIndex = (currentIndex + 1) % kanjiList.size();
                        fillLabelsWithKanji();
                    }
                    else {
                        showAnswer();
                    }
                }
            }
            public void onSwipeLeft() {
                if(swipeMode) {
                    if(revealed) {
                        currentIndex = (currentIndex - 1 < 0) ? kanjiList.size() - 1 : currentIndex - 1;
                        fillLabelsWithKanji();
                    }
                    else {
                        showAnswer();
                    }
                }
            }
            public void onSwipeBottom() {
                if(swipeMode) {
                    showAnswer();
                }
            }
        };
        main.setOnTouchListener(swiperListener);
        createTimer(settings, bar_timer);

        lbl_paused.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerPaused = !timerPaused;
                timerHandler.postDelayed(timerRunnable, 1000);
                Toast.makeText(MainActivity.this, (timerPaused) ? getString(R.string.paused) : getString(R.string.resume), Toast.LENGTH_SHORT).show();
            }
        });

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
        currentIndex = 0;
        errorActive = false;
        editSizes = getIntent().getBooleanExtra("incoming_size", false);
        SharedPreferences settings = getSharedPreferences(SettingsActivity.PREFERENCES_FILE_NAME, 0);
        applyTheme(settings);

        super.onResume();

        initializeKanji();
        lbl_furigana.setTextSize(TypedValue.COMPLEX_UNIT_DIP, settings.getInt("sizeFurigana", SIZE_FURIGANA));
        lbl_kanji.setTextSize(TypedValue.COMPLEX_UNIT_DIP, settings.getInt("sizeKanji", SIZE_KANJI));
        lbl_meaning.setTextSize(TypedValue.COMPLEX_UNIT_DIP, settings.getInt("sizeMeaning", SIZE_MEANING));
        lbl_difficulty.setTextSize(TypedValue.COMPLEX_UNIT_DIP, settings.getInt("sizeDifficulty", SIZE_DIFFICULTY));

        if(!errorActive) {
            initializeViews(settings);
            hideAnswer();
            setQuestionMode(settings);
            setInputMode(settings);
        }
        if(editSizes)
            initializeSizeSliders(settings);
    }

    private void initializeViews(SharedPreferences settings) {
        showFurigana = settings.getBoolean("furiganaActive", true);
        showKanji = settings.getBoolean("kanjiActive", true);
        showMeaning = settings.getBoolean("meaningActive", true);
        showDifficulty = settings.getBoolean("difficultyActive", true);
        con_menuButtons.setVisibility(settings.getBoolean("showMenuButtons", true) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
            bar_timer.setProgress(0);
            timerIndex = 0;
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
        FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(MainActivity.this);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery(FeedReaderContract.SQL_TABLE_EXISTS, null);
        if(c!=null) {
            if(c.getCount() <= 0) {
                db.execSQL(FeedReaderContract.SQL_CREATE_ENTRIES);
            }
            c.close();
        }
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

    private void initializeKanji() {
        kanjiList = getKanjiFromDatabase();
        Log.d("array", "array: " + Arrays.toString(kanjiArray));
        if(kanjiArray.length > 0) {
            fillLabelsWithKanji();
        }
        else {
            fillLabelsWithKanji();

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

    private void fillLabelsWithKanji() {
        if(kanjiList != null) {
            if (kanjiList.size() > 0) {
                currentKanji = kanjiList.get(kanjiArray[currentIndex]);

                //Toast.makeText(MainActivity.this, "idx: " + kanjiList.get(kanjiArray[currentIndex]).getKanji() + " " + currentKanji.getKanji(), Toast.LENGTH_SHORT).show();

                lbl_kanji.setText(currentKanji.getKanji());
                lbl_furigana.setText(currentKanji.getFurigana());
                lbl_meaning.setText(currentKanji.getMeaning());
                lbl_difficulty.setText(currentKanji.getDifficulty() + "/9");
                lbl_paused.setVisibility(View.GONE);
            }
            else {
                fatalError(getString(R.string.errorKanjiListEmpty));
            }
        }
        else {
            fatalError(getString(R.string.errorUnableToLoadKanji));
        }
    }

    private void showAnswer() {
        lbl_furigana.setVisibility(View.VISIBLE);
        lbl_kanji.setVisibility(View.VISIBLE);
        lbl_meaning.setVisibility(View.VISIBLE);
        btn_no.setEnabled(true);
        btn_yes.setEnabled(true);
        tbx_input.setEnabled(false);
        checkInputBoxValue();
        revealed = true;
    }

    private void hideAnswer() {
        lbl_furigana.setVisibility((showFurigana) ? View.VISIBLE : View.INVISIBLE);
        lbl_kanji.setVisibility((showKanji) ? View.VISIBLE : View.INVISIBLE);
        lbl_meaning.setVisibility((showMeaning) ? View.VISIBLE : View.INVISIBLE);
        lbl_difficulty.setVisibility((showDifficulty) ? View.VISIBLE : View.INVISIBLE);
        tbx_input.setText("");
        btn_no.setEnabled(false);
        btn_yes.setEnabled(false);
        tbx_input.setEnabled(true);
        revealed = false;
    }

    private void setQuestionMode(SharedPreferences settings) {
        long mode = settings.getLong("questionMode", 0L);
        bar_timer.setVisibility(mode == 0 ? View.VISIBLE : View.INVISIBLE);
        lbl_paused.setVisibility(mode == 0 ? View.VISIBLE : View.INVISIBLE);
        swipeMode = (mode == 1);
        btn_reveal.setVisibility(mode == 2 ? View.VISIBLE : View.GONE);
    }

    private void setInputMode(SharedPreferences settings) {
        long mode = settings.getLong("inputMode", 0L);
        tbx_input.setVisibility((mode == 0) ? View.VISIBLE : View.GONE);
        inputMode = (mode == 0);
        inputModeType = settings.getLong("inputModeType", 0L);
        con_yesNoButtons.setVisibility((mode == 1) ? View.VISIBLE : View.GONE);
    }

    private boolean checkInputBox() {
        String check = "";
        if(inputModeType == 0) { // Furigana
            check = currentKanji.getFurigana();
        }
        else if(inputModeType == 1) { // Kanji
            check = currentKanji.getKanji();
        }
        else if(inputModeType == 2) { // meaning
            check = currentKanji.getMeaning().toLowerCase();
            return (tbx_input.getText().toString().toLowerCase().equals(check));
        }
        return (tbx_input.getText().toString().equals(check));
    }

    private void checkInputBoxValue() {
        if(inputMode) {
            alterDifficulty((checkInputBox() ? -1 : 1), currentKanji);
        }
    }

    private void alterDifficulty(int modifier, Kanji kanji) {
        Toast.makeText(MainActivity.this, getString((modifier < 0) ? R.string.correct : R.string.wrong), Toast.LENGTH_SHORT).show();
        kanji.changeDifficulty(modifier);

        FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(MainActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DIFFICULTY, kanji.getDifficulty());

        db.update(FeedReaderContract.FeedEntry.TABLE_NAME, values, " id=" + kanji.getKanjiID(), null);
        lbl_difficulty.setText(kanji.getDifficulty() + "/9");
    }

    private void createTimer(SharedPreferences settings, final ProgressBar bar_timer) {
        timerMaxReveal = settings.getInt("secondsToReveal", 7);
        timerMaxNext = settings.getInt("secondsToNext", 3);
        bar_timer.setMax(timerMaxReveal);

        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                lbl_paused.setVisibility(View.INVISIBLE);
                bar_timer.setProgress(timerIndex);
                timerIndex++;

                if (!revealed) {
                    if (timerIndex > timerMaxReveal + 1) {
                        revealed = false;
                        bar_timer.setMax(timerMaxNext);
                        bar_timer.setProgress(0);
                        timerIndex = 0;
                        showAnswer();
                    }
                }
                else {
                    if (timerIndex > timerMaxNext + 1) {
                        fillLabelsWithKanji();
                        revealed = true;
                        bar_timer.setMax(timerMaxReveal);
                        bar_timer.setProgress(0);
                        timerIndex = 0;
                        hideAnswer();
                        currentIndex = (currentIndex + 1) % kanjiList.size();
                    }
                }
                timerHandler.postDelayed(this, 1000);
            }
        };
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

    static void FisherYatesShuffleArray(int[] array) {
        int n = array.length;
        for (int i = 0; i < array.length; i++) {
            int random = i + (int) (Math.random() * (n - i));
            int randomElement = array[random];
            array[random] = array[i];
            array[i] = randomElement;
        }
    }

    private void fatalError(String message) {
        errorActive = true;
        lbl_errors.setText(message);
        lbl_errors.setVisibility(View.VISIBLE);
        bar_timer.setVisibility(View.GONE);
        lbl_paused.setVisibility(View.GONE);
        con_yesNoButtons.setVisibility(View.GONE);
        btn_reveal.setVisibility(View.GONE);
        tbx_input.setVisibility(View.GONE);
        lbl_furigana.setVisibility(View.GONE);
        lbl_kanji.setVisibility(View.GONE);
        lbl_meaning.setVisibility(View.GONE);
        lbl_difficulty.setVisibility(View.GONE);
    }

    private void initializeSizeSliders(final SharedPreferences settings) {
        RelativeLayout container = (RelativeLayout)findViewById(R.id.con_sizeSliders);
        container.setVisibility(View.VISIBLE);
        con_yesNoButtons.setVisibility(View.GONE);
        con_menuButtons.setVisibility(View.GONE);
        bar_timer.setVisibility(View.GONE);
        tbx_input.setVisibility(View.GONE);
        btn_reveal.setVisibility(View.GONE);
        lbl_furigana.setVisibility(View.VISIBLE);
        lbl_kanji.setVisibility(View.VISIBLE);
        lbl_meaning.setVisibility(View.VISIBLE);
        lbl_difficulty.setVisibility(View.VISIBLE);

        SeekBar sld_sizeFurigana = (SeekBar)findViewById(R.id.sld_sizeFurigana);
        SeekBar sld_sizeKanji = (SeekBar)findViewById(R.id.sld_sizeKanji);
        SeekBar sld_sizeMeaning = (SeekBar)findViewById(R.id.sld_sizeMeaning);
        SeekBar sld_sizeDifficulty = (SeekBar)findViewById(R.id.sld_sizeDifficulty);

        setSeekBar(sld_sizeFurigana, lbl_furigana, "sizeFurigana", settings);
        setSeekBar(sld_sizeKanji, lbl_kanji, "sizeKanji", settings);
        setSeekBar(sld_sizeMeaning, lbl_meaning, "sizeMeaning", settings);
        setSeekBar(sld_sizeDifficulty, lbl_difficulty, "sizeDifficulty", settings);

        sld_sizeFurigana.setProgress(settings.getInt("sizeFurigana", SIZE_FURIGANA));
        sld_sizeKanji.setProgress(settings.getInt("sizeKanji", SIZE_KANJI));
        sld_sizeMeaning.setProgress(settings.getInt("sizeMeaning", SIZE_DIFFICULTY));
        sld_sizeDifficulty.setProgress(settings.getInt("sizeDifficulty", SIZE_MEANING));
    }

    private void setSeekBar(SeekBar seekbar, final TextView label, final String preference, final SharedPreferences settings) {
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, progress + 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(preference, seekBar.getProgress() + 1);
                editor.commit();
            }
        });
    }
}
