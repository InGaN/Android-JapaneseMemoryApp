package com.example.kevin.japanesememoryapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    FeedReaderDbHelper dbHelper;
    ListView kanjiList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        fillListWithKanji(getKanjiFromDatabase());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_list, menu);
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

    private void fillListWithKanji(final ArrayList<Kanji> kanji) {
        TextView lbl_emptyList = (TextView)findViewById(R.id.lbl_emptyList);
        lbl_emptyList.setVisibility((kanji.size() > 0) ? View.GONE : View.VISIBLE);
        if(kanji.size() > 0) {
            CustomListAdapter customListAdapter = new CustomListAdapter(this, kanji);

            kanjiList = (ListView) findViewById(R.id.lst_kanjiList);
            kanjiList.setAdapter(customListAdapter);
        }
        else {
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
            AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
            builder.setMessage(getString(R.string.mainEmptyListMessage)).setPositiveButton(getString(R.string.mainAddNew), dialogClickListener).setNegativeButton(getString(R.string.mainCancel), dialogClickListener).show();
        }
    }

    private void callInputActivity() {
        Intent intent = new Intent(this, InputActivity.class);
        startActivity(intent);
    }

    private ArrayList<Kanji> getKanjiFromDatabase() {
        dbHelper = new FeedReaderDbHelper(ListActivity.this);

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
                FeedReaderContract.FeedEntry.TABLE_NAME,                    // The table to query
                selectQuery,                                                // The columns to return
                null,                                                       // The columns for the WHERE clause
                null,                                                       // The values for the WHERE clause
                null,                                                       // don't group the rows
                null,                                                       // don't filter by row groups
                sortOrder                                                   // The sort order
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
}
