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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.myKanji.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ListActivity extends AppCompatActivity {
    FeedReaderDbHelper dbHelper;
    ListView kanjiList;
    ProgressBar loadProgress;
    CustomListAdapter customListAdapter;
    ArrayList<Kanji> kanji = new ArrayList<>();
    boolean sortDifficulty;
    boolean sortID;
    int totalKanji = 0;
    int loadedKanji = 0;
    final int loadLimit = 200;
    public static final int FILE_SELECT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = getSharedPreferences(SettingsActivity.PREFERENCES_FILE_NAME, 0);
        applyTheme(settings);
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.activity_list);
        loadProgress = (ProgressBar) findViewById(R.id.load_progress);
        kanjiList = (ListView) findViewById(R.id.lst_kanjiList);
        totalKanji = getTotalKanji();

        Intent intent = getIntent();
        if(intent.getExtras() != null) {
            if (!intent.getStringExtra("str_furigana").equals("") || !intent.getStringExtra("str_kanji").equals("") || !intent.getStringExtra("str_meaning").equals("")) {
                searchKanji(intent.getStringExtra("str_furigana"), intent.getStringExtra("str_kanji"), intent.getStringExtra("str_meaning"));
            }
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
    public void onResume() {
        super.onResume();
        loadedKanji = 0;
        totalKanji = getTotalKanji();
        Intent intent = getIntent();
        if(intent.getExtras() == null) {
            kanji = getKanjiFromDatabase(FeedReaderContract.FeedEntry.COLUMN_NAME_ID, sortID, "0, " + loadLimit);
            fillListWithKanji();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_searchKanji:
                callInputActivity(true);
                return true;
            case R.id.action_input:
                callInputActivity(false);
                return true;
            case R.id.action_sortDifficulty:
                sortDifficulty = !sortDifficulty;
                kanji = getKanjiFromDatabase(FeedReaderContract.FeedEntry.COLUMN_NAME_DIFFICULTY , sortDifficulty, null);
                fillListWithKanji();
                return true;
            case R.id.action_clearDatabase:
                clearDatabase();
                return true;
            case R.id.action_sortID:
                sortID = !sortID;
                kanji = getKanjiFromDatabase(FeedReaderContract.FeedEntry.COLUMN_NAME_ID , sortID, null);
                fillListWithKanji();
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

    private void fillListWithKanji() {
        TextView lbl_emptyList = (TextView)findViewById(R.id.lbl_emptyList);
        lbl_emptyList.setVisibility((kanji.size() > 0) ? View.GONE : View.VISIBLE);

        if(kanji.size() > 0) {
            customListAdapter = new CustomListAdapter(this, kanji);

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

            kanjiList.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) { }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0) {
                        if(totalKanji > loadedKanji) {
                            loadMoreKanji();
                            //MainActivity.showAlert(ListActivity.this, "TEST", "TEST");
                        }
                    }
                }
            });
        }
        else {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            callInputActivity(false);
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

    private void callInputActivity(boolean search) {
        Intent intent = new Intent(this, InputActivity.class);
        if(search) {
            intent.putExtra("searching", true);
        }
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

    private int getTotalKanji() {
        dbHelper = new FeedReaderDbHelper(ListActivity.this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(" + FeedReaderContract.FeedEntry.COLUMN_NAME_ID + ") FROM " + FeedReaderContract.FeedEntry.TABLE_NAME, null);
        cursor.moveToFirst();
        return cursor.getInt(0);
    }


    private ArrayList<Kanji> getKanjiFromDatabase(String sortType, boolean asc, String amount) {
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
                sortOrder,                                                  // The sort order
                amount                                                      // Limit, remove argument to select all
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

    private void loadMoreKanji() {
            loadProgress.setVisibility(View.VISIBLE);
            loadedKanji += loadLimit;
            appendKanji(getKanjiFromDatabase(FeedReaderContract.FeedEntry.COLUMN_NAME_ID, sortID, loadedKanji + ", " + loadLimit));
            loadProgress.setVisibility(View.GONE);
    }

    private void appendKanji(final ArrayList<Kanji> newKanji) {
        for(Kanji word : newKanji) {
            kanji.add(word);
        }
        customListAdapter.notifyDataSetChanged();
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
                        //Log.d("TEST", "File Path: " + getPath(this, uri).toString());
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

    private boolean searchKanji(String furigana, String word, String meaning) {
        ProgressBar loader = (ProgressBar) findViewById(R.id.load_progress);
        loader.setVisibility(View.VISIBLE);

        ArrayList<Kanji> availableKanji = getKanjiFromDatabase(FeedReaderContract.FeedEntry.COLUMN_NAME_ID, sortID, null);
        HashMap<Kanji, Integer> map = new HashMap<>();

        for (int x = 0; x < availableKanji.size(); x++) {
            Kanji currentKanji = availableKanji.get(x);
            if (currentKanji.getFurigana().contains(furigana) && !furigana.equals("")) { // messy, reoccurring code
                if (map.keySet().contains(currentKanji)) {
                    map.put(currentKanji, map.get(currentKanji) + 1);
                } else {
                    map.put(currentKanji, 0);
                }
            }
            if (currentKanji.getKanji().contains(word) && !word.equals("")) {
                if (map.keySet().contains(currentKanji)) {
                    map.put(currentKanji, map.get(currentKanji) + 1);
                } else {
                    map.put(currentKanji, 0);
                }
            }
            if (currentKanji.getMeaning().contains(meaning) && !meaning.equals("")) {
                if (map.keySet().contains(currentKanji)) {
                    map.put(currentKanji, map.get(currentKanji) + 1);
                } else {
                    map.put(currentKanji, 0);
                }
            }
        }

        Log.d("MAP", map.size() + " | " + map.toString());

        ArrayList<Kanji> foundKanji = new ArrayList<>();
        int maxValue = 0;
        for (Integer val : map.values()) {
            if (val > maxValue)
                maxValue = val;
        }

        for (int x = maxValue; x >= 0; x--) {
            for (Map.Entry<Kanji, Integer> entry : map.entrySet()) {
                if (entry.getValue() == x)
                    foundKanji.add(entry.getKey());
            }
        }

        loader.setVisibility(View.GONE);

        if (foundKanji.size() > 0) {
            kanji = foundKanji;
            fillListWithKanji();
            return true;
        }
        else {
            MainActivity.showAlert(ListActivity.this, getString(R.string.error), getString(R.string.inputSearchFailed));
            return false;
        }
    }
}
