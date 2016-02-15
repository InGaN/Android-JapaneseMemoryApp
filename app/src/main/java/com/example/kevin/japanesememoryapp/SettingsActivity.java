package com.example.kevin.japanesememoryapp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    public static final String PREFERENCES_FILE_NAME = "MyPreferences";
    Switch swc_timer;
    Switch swc_furigana;
    Switch swc_kanji;
    Switch swc_meaning;
    Switch swc_difficulty;
    Spinner spn_questionMode;

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

        setSpinners(settings);
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

    private void setSpinners(SharedPreferences settings) {
        spn_questionMode = (Spinner)findViewById(R.id.spn_questionMode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.questionModes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_questionMode.setAdapter(adapter);

        spn_questionMode.setSelection((int)settings.getLong("questionMode", 0L));

        spn_questionMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putLong("questionMode", id);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setSwitches(SharedPreferences settings) {
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

        swc_difficulty = (Switch)findViewById(R.id.swc_difficulty);
        swc_difficulty.setChecked(settings.getBoolean("difficultyActive", true));
        swc_difficulty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("difficultyActive", isChecked);
                editor.commit();
            }
        });
    }
}
