package com.example.kevin.japanesememoryapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    FeedReaderDbHelper dbHelper;
    TextView lbl_kanji;
    TextView lbl_furigana;
    TextView lbl_meaning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lbl_kanji = (TextView)findViewById(R.id.lbl_kanji);
        lbl_furigana = (TextView)findViewById(R.id.lbl_furigana);
        lbl_meaning = (TextView)findViewById(R.id.lbl_meaning);

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

        displayKanji(getKanjiFromDatabase());
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        String sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_ID + " DESC";

        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                selectQuery,
                FeedReaderContract.FeedEntry.COLUMN_NAME_ID + "=?",
                new String[]{"1"},
                null,
                null,
                sortOrder
        );

        if(cursor != null) {
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
            return kanji;
        }
        return null;
    }

    private void displayKanji(ArrayList<Kanji> kanjiList) {
        if(kanjiList != null) {
            if (kanjiList.size() > 0) {
                Kanji currentKanji = kanjiList.get(0);
                lbl_kanji.setText(currentKanji.getKanji());
                lbl_furigana.setText(currentKanji.getFurigana());
                lbl_meaning.setText(currentKanji.getMeaning());
            }
            else {
                lbl_kanji.setText("");
                lbl_furigana.setText("");
                lbl_meaning.setText("Kanji list is empty...");
            }
        }
        else {
            lbl_kanji.setText("");
            lbl_furigana.setText("");
            lbl_meaning.setText("Unable to load Kanji...");
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
        Intent intent = new Intent(this, InputActivity.class);
        startActivity(intent);
    }

    private void callKanjiList() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }
}
