package com.myKanji;

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.myKanji.R;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    FeedReaderDbHelper dbHelper;
    ListView kanjiList;
    boolean sortDifficulty;
    boolean sortID;
    int totalKanji = 0;
    public static final int FILE_SELECT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = getSharedPreferences(SettingsActivity.PREFERENCES_FILE_NAME, 0);
        applyTheme(settings);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);
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
    public void onResume() {
        super.onResume();
        fillListWithKanji(getKanjiFromDatabase(FeedReaderContract.FeedEntry.COLUMN_NAME_ID, sortID));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_input:
                callInputActivity();
                return true;
            case R.id.action_sortDifficulty:
                sortDifficulty = !sortDifficulty;
                fillListWithKanji(getKanjiFromDatabase(FeedReaderContract.FeedEntry.COLUMN_NAME_DIFFICULTY , sortDifficulty));
                return true;
            case R.id.action_clearDatabase:
                clearDatabase();
                return true;
            case R.id.action_sortID:
                sortID = !sortID;
                fillListWithKanji(getKanjiFromDatabase(FeedReaderContract.FeedEntry.COLUMN_NAME_ID , sortID));
                return true;
            case R.id.action_exportDatabase:
                if(totalKanji > 0)
                    MainActivity.showAlert(ListActivity.this, getString(R.string.action_exportDatabase), dbHelper.exportDatabase() ? totalKanji + " " + getString(R.string.db_export_good) : getString(R.string.db_export_fail));
                else
                    MainActivity.showAlert(ListActivity.this, getString(R.string.action_exportDatabase), getString(R.string.db_export_none));
                return true;
            case R.id.action_importDatabase:
                importDatabase();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void fillListWithKanji(final ArrayList<Kanji> kanji) {
        TextView lbl_emptyList = (TextView)findViewById(R.id.lbl_emptyList);
        lbl_emptyList.setVisibility((kanji.size() > 0) ? View.GONE : View.VISIBLE);

        if(kanji.size() > 0) {
            CustomListAdapter customListAdapter = new CustomListAdapter(this, kanji);
            totalKanji = kanji.size();

            kanjiList = (ListView) findViewById(R.id.lst_kanjiList);
            kanjiList.setAdapter(customListAdapter);

            kanjiList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    final int index = position;
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    editKanji(kanji.get(index));
                                    break;
                                case DialogInterface.BUTTON_NEUTRAL:
                                    deleteKanji(index, kanji);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    //Do nothing, close dialog
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                    builder.setMessage("ID:" + kanji.get(position).getKanjiID() + " " + kanji.get(position).getKanji())
                            .setPositiveButton(getString(R.string.edit), dialogClickListener)
                            .setNegativeButton(getString(R.string.cancel), dialogClickListener)
                            .setNeutralButton(getString(R.string.delete), dialogClickListener).show();
                    return false;
                }
            });
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
            builder.setMessage(getString(R.string.mainEmptyListMessage)).setPositiveButton(getString(R.string.addNew), dialogClickListener).setNegativeButton(getString(R.string.cancel), dialogClickListener).show();
        }
    }

    private void editKanji(Kanji kanji){
        Intent intent = new Intent(this, InputActivity.class);
        intent.putExtra("incoming_kanji", kanji);
        startActivity(intent);
    }

    private void callInputActivity() {
        Intent intent = new Intent(this, InputActivity.class);
        startActivity(intent);
    }

    private void deleteKanji(final int key, final ArrayList<Kanji> kanji) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dbHelper = new FeedReaderDbHelper(ListActivity.this);
                        SQLiteDatabase db = dbHelper.getWritableDatabase();

                        db.delete(
                                FeedReaderContract.FeedEntry.TABLE_NAME,
                                FeedReaderContract.FeedEntry.COLUMN_NAME_ID + "=" + kanji.get(key).getKanjiID(),
                                null
                        );
                        finish();
                        startActivity(getIntent());
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //Do nothing, close dialog
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
        builder.setMessage(getString(R.string.mainAreYouSure) + " ID: " + kanji.get(key).getKanjiID() + " " + kanji.get(key).getKanji() + "?").setPositiveButton(getString(R.string.delete), dialogClickListener).setNegativeButton(getString(R.string.cancel), dialogClickListener).show();
    }

    private void clearDatabase() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dbHelper = new FeedReaderDbHelper(ListActivity.this);
                        SQLiteDatabase db = dbHelper.getWritableDatabase();

                        db.execSQL(FeedReaderContract.SQL_DELETE_ENTRIES);
                        db.execSQL(FeedReaderContract.SQL_CREATE_ENTRIES);

                        finish();
                        startActivity(getIntent());
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //Do nothing, close dialog
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
        builder.setMessage(getString(R.string.areYouSureClear)).setPositiveButton(getString(R.string.yes), dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    private ArrayList<Kanji> getKanjiFromDatabase(String sortType, boolean asc) {
        dbHelper = new FeedReaderDbHelper(ListActivity.this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectQuery = {
                FeedReaderContract.FeedEntry.COLUMN_NAME_ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_KANJI,
                FeedReaderContract.FeedEntry.COLUMN_NAME_FURIGANA,
                FeedReaderContract.FeedEntry.COLUMN_NAME_MEANING,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DIFFICULTY
        };

        String sortOrder = sortType + ((asc) ? " ASC" : " DESC");

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

    private void importDatabase() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);

                        try {
                            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
                        }
                        catch (android.content.ActivityNotFoundException ex) {
                            //Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //Do nothing, close dialog
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
        builder.setMessage(getString(R.string.areYouSureImport)).setPositiveButton(getString(R.string.continew), dialogClickListener).setNegativeButton(getString(R.string.cancel), dialogClickListener).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Log.d("TEST", "File Uri: " + uri.toString());
                    try {
                        Log.d("TEST", "File Path: " + getPath(this, uri).toString());
                        File file = new File(getPath(this, uri));
                        MainActivity.showAlert(ListActivity.this, "result", "Import: " + dbHelper.importDatabase(file));
                    }
                    catch(URISyntaxException e) {
                        Log.d("TEST", "Error getting URI");
                    }
                    catch(IllegalArgumentException e) {
                        Log.d("TEST", "Error importing database from this file");
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
}
