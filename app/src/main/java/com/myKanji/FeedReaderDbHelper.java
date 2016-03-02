package com.myKanji;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by kevin on 2016/02/14.
 */
public class FeedReaderDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Kanji.db";
    public static final String DATABASE_PATH = "/data/"+ MainActivity.PACKAGE_NAME +"/databases/";

    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FeedReaderContract.SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(FeedReaderContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean exportDatabase() {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        String currentDBPath = DATABASE_PATH + DATABASE_NAME;

        File folder = new File(Environment.getExternalStorageDirectory() + "/MyKanji");
        if (!folder.exists())
            folder.mkdir();

        String backupDBPath = "MyKanji/" + DATABASE_NAME;
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            return true;
        } catch(IOException e) {
            Log.d("EXPORT", e.toString());
            e.printStackTrace();
        }
        return false;
    }

    public String importDatabase(File sourceFile) throws IllegalArgumentException {
        //// TODO: 2016/02/24 check if db is valid
        try {
            //// TODO: 2016/02/25 Merge database
            //SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            //Cursor cursor = db.query(true, FeedReaderContract.FeedEntry.TABLE_NAME, null, null, null, null, null, null, null);
            File destFile = new File("data" + DATABASE_PATH + DATABASE_NAME);
            InputStream inputStream = new FileInputStream(sourceFile);
            OutputStream outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            inputStream.close();
            outputStream.close();
            return "OK";
        }
        catch(Exception e) {
            Log.d("TEST", e.toString());
            e.printStackTrace();
            return e.toString();
        }
    }
}
