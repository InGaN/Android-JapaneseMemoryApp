package com.myKanji;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.myKanji.R;

public class InputActivity extends AppCompatActivity {
    FeedReaderDbHelper dbHelper;

    TextView lbl_addKanji;
    EditText tbx_furigana;
    EditText tbx_kanji;
    EditText tbx_meaning;

    Kanji currentKanji;
    boolean searchActive;
    private int defaultDifficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = getSharedPreferences(SettingsActivity.PREFERENCES_FILE_NAME, 0);
        applyTheme(settings);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        defaultDifficulty = settings.getInt("defaultDifficulty", SettingsActivity.DEFAULT_DIFFICULTY);

        lbl_addKanji = (TextView)findViewById(R.id.lbl_addKanji);
        tbx_furigana = (EditText)findViewById(R.id.tbx_addFurigana);
        tbx_kanji = (EditText)findViewById(R.id.tbx_addKanji);
        tbx_meaning = (EditText)findViewById(R.id.tbx_addMeaning);
        Button btn_addKanji = (Button)findViewById(R.id.btn_addNewKanji);
        Button btn_editKanji = (Button)findViewById(R.id.btn_editKanji);
        Button btn_searchKanji = (Button)findViewById(R.id.btn_searchKanji);

        btn_addKanji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verifyInput()) {
                    if(checkKanjiExists(tbx_kanji.getText().toString())) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        saveKanji();
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //Do nothing, close dialog
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);
                        builder.setMessage(getString(R.string.inputKanjiExists) + " " + tbx_kanji.getText().toString() + "\n\n" + getString(R.string.inputAddAnyway)).setPositiveButton(getString(R.string.yes), dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show();
                    }
                    else {
                        saveKanji();
                    }
                }
                else {
                    MainActivity.showAlert(InputActivity.this, getString(R.string.error), getString(R.string.inputFillInAllPlease));
                }
            }
        });

        dbHelper = new FeedReaderDbHelper(InputActivity.this);


        Intent intent = getIntent();
        currentKanji = (Kanji)intent.getParcelableExtra("incoming_kanji");
        searchActive = intent.getBooleanExtra("searching", false);

        if(currentKanji != null) {
            btn_addKanji.setVisibility(View.GONE);
            btn_searchKanji.setVisibility(View.GONE);
            btn_editKanji.setVisibility(View.VISIBLE);

            lbl_addKanji.setText(getString(R.string.inputEditKanji) + " ID:" + currentKanji.getKanjiID());
            tbx_furigana.setText(currentKanji.getFurigana());
            tbx_kanji.setText(currentKanji.getKanji());
            tbx_meaning.setText(currentKanji.getMeaning());

            btn_editKanji.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateKanji(currentKanji.getKanjiID());
                }
            });
        }
        else if(searchActive) {
            btn_addKanji.setVisibility(View.GONE);
            btn_editKanji.setVisibility(View.GONE);
            btn_searchKanji.setVisibility(View.VISIBLE);

            lbl_addKanji.setText(getString(R.string.search));

            btn_searchKanji.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callListActivitySearch();
                }
            });
        }
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
                setTheme(R.style.AppThemeBlue);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    private void saveKanji() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_FURIGANA, tbx_furigana.getText().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_KANJI, tbx_kanji.getText().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_MEANING, tbx_meaning.getText().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DIFFICULTY, defaultDifficulty);

        long newRowId;
        newRowId = db.insert(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                FeedReaderContract.FeedEntry.COLUMN_NAME_KANJI,
                values
        );
        if(newRowId == -1) {
            MainActivity.showAlert(InputActivity.this, getString(R.string.error), getString(R.string.inputUnableToAdd));
        }
        else {
            MainActivity.showAlert(InputActivity.this, getString(R.string.inputNewKanjiAdded), "(id: " + newRowId + ")");
            tbx_furigana.setText("");
            tbx_kanji.setText("");
            tbx_meaning.setText("");
        }
    }

    private void updateKanji(int index) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_FURIGANA, tbx_furigana.getText().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_KANJI, tbx_kanji.getText().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_MEANING, tbx_meaning.getText().toString());

        if(db.update(FeedReaderContract.FeedEntry.TABLE_NAME, values, " id=" + index, null) > 0) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            finish();
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);
            builder.setMessage(getString(R.string.inputUpdateSuccess)).setPositiveButton(getString(R.string.ok), dialogClickListener).show();
        }
        else {
            MainActivity.showAlert(InputActivity.this, getString(R.string.error), getString(R.string.inputUpdateWrong));
        }
    }

    private boolean checkKanjiExists(String input) {
        dbHelper = new FeedReaderDbHelper(InputActivity.this);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectQuery = {
                FeedReaderContract.FeedEntry.COLUMN_NAME_KANJI
        };

        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                selectQuery,
                FeedReaderContract.FeedEntry.COLUMN_NAME_KANJI + "=?",
                new String[]{input},
                null,
                null,
                ""
        );

        return (cursor.getCount() > 0);
    }

    private boolean verifyInput() {
        if(tbx_furigana.getText().toString().equals("")){
            return false;
        }
        if(tbx_kanji.getText().toString().equals("")){
            return false;
        }
        if(tbx_meaning.getText().toString().equals("")){
            return false;
        }
        return true;
    }

    private void callListActivitySearch() {
        if(!tbx_furigana.getText().toString().equals("") || !tbx_kanji.getText().toString().equals("") || !tbx_meaning.getText().toString().equals("")) {
            Intent intent = new Intent(this, ListActivity.class);
            intent.putExtra("str_furigana", tbx_furigana.getText().toString());
            intent.putExtra("str_kanji", tbx_kanji.getText().toString());
            intent.putExtra("str_meaning", tbx_meaning.getText().toString());
            startActivity(intent);
        }
        else {
            MainActivity.showAlert(InputActivity.this, getString(R.string.error), getString(R.string.inputAtLeastOne));
        }
    }
}
