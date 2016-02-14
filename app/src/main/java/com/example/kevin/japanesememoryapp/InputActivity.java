package com.example.kevin.japanesememoryapp;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class InputActivity extends AppCompatActivity {
    public static int DEFAULT_DIFFICULTY = 5;

    FeedReaderDbHelper dbHelper;

    EditText tbx_furigana;
    EditText tbx_kanji;
    EditText tbx_meaning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        tbx_furigana = (EditText)findViewById(R.id.tbx_addFurigana);
        tbx_kanji = (EditText)findViewById(R.id.tbx_addKanji);
        tbx_meaning = (EditText)findViewById(R.id.tbx_addMeaning);

        Button btn_addKanji = (Button)findViewById(R.id.btn_addNewKanji);
        btn_addKanji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verifyInput()) {
                    saveKanji();
                }
                else {
                    MainActivity.showAlert(InputActivity.this, "ERROR", "Fill in all input areas");
                }
            }
        });

        dbHelper = new FeedReaderDbHelper(InputActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_input, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveKanji() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_FURIGANA, tbx_furigana.getText().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_KANJI, tbx_kanji.getText().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_MEANING, tbx_meaning.getText().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DIFFICULTY, DEFAULT_DIFFICULTY);

        long newRowId;
        newRowId = db.insert(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                FeedReaderContract.FeedEntry.COLUMN_NAME_KANJI,
                values
        );
        if(newRowId == -1) {
            MainActivity.showAlert(InputActivity.this, "ERROR", "Unable to enter new kanji to list...");
        }
        else {
            MainActivity.showAlert(InputActivity.this, "New kanji added to list", "(id: " + newRowId + ")");
            tbx_furigana.setText("");
            tbx_kanji.setText("");
            tbx_meaning.setText("");
        }

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
}
