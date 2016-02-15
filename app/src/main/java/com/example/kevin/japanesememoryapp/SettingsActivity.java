package com.example.kevin.japanesememoryapp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    public static final String PREFERENCES_FILE_NAME = "MyPreferences";
    Switch swc_timer;
    Switch swc_furigana;
    Switch swc_kanji;
    Switch swc_meaning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);

        TextView lbl_about = (TextView)findViewById(R.id.lbl_settingsAbout);
        lbl_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.showAlert(SettingsActivity.this, "About", "Version: 0.1");
            }
        });

        setSwitches(settings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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

    private void setSwitches(SharedPreferences settings) {
        swc_timer = (Switch)findViewById(R.id.swc_timer);
        swc_timer.setChecked(settings.getBoolean("timerActive", true));
        swc_timer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("timerActive", isChecked);
                editor.commit();
            }
        });

        swc_furigana = (Switch)findViewById(R.id.swc_furigana);
        swc_furigana.setChecked(settings.getBoolean("furiganaActive", true));
        swc_furigana.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("furiganaActive", isChecked);
                editor.commit();
            }
        });

        swc_kanji = (Switch)findViewById(R.id.swc_kanji);
        swc_kanji.setChecked(settings.getBoolean("kanjiActive", true));
        swc_kanji.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("kanjiActive", isChecked);
                editor.commit();
            }
        });

        swc_meaning = (Switch)findViewById(R.id.swc_meaning);
        swc_meaning.setChecked(settings.getBoolean("meaningActive", true));
        swc_meaning.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("meaningActive", isChecked);
                editor.commit();
            }
        });
    }
}
