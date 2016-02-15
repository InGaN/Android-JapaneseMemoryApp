package com.example.kevin.japanesememoryapp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    public static final String PREFERENCES_FILE_NAME = "MyPreferences";
    Switch swc_furigana;
    Switch swc_kanji;
    Switch swc_meaning;
    Switch swc_difficulty;
    Spinner spn_questionMode;
    EditText tbx_secondsReveal;
    EditText tbx_secondsNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);

        TextView lbl_about = (TextView)findViewById(R.id.lbl_settingsAbout);
        lbl_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.showAlert(SettingsActivity.this, getString(R.string.aboutTitle), getString(R.string.aboutMessage) +"\r"+ getString(R.string.aboutWebsite));
            }
        });

        initializeItems(settings);
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

    private void initializeItems(SharedPreferences settings) {
        setSpinners(settings);
        setEditTexts(settings);
        setSwitches(settings);
    }

    private void setSpinners(SharedPreferences settings) {
        spn_questionMode = (Spinner)findViewById(R.id.spn_questionMode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.questionModes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_questionMode.setAdapter(adapter);

        spn_questionMode.setSelection((int) settings.getLong("questionMode", 0L));

        spn_questionMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putLong("questionMode", id);
                editor.commit();
                visibleTimerOptions((id == 0));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setEditTexts(SharedPreferences settings) {
        tbx_secondsReveal = (EditText)findViewById(R.id.tbx_timerReveal);
        tbx_secondsReveal.setText(String.valueOf(settings.getInt("secondsToReveal", 1)));
        tbx_secondsReveal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().equals("")) {
                    if(Integer.parseInt(s.toString()) > 0) {
                        SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("secondsToReveal", Integer.parseInt(s.toString()));
                        editor.commit();
                    }
                    else {
                        MainActivity.showAlert(SettingsActivity.this, getString(R.string.error), getString(R.string.settingsNoZero));
                    }
                }
            }
        });

        tbx_secondsNext = (EditText)findViewById(R.id.tbx_timerNext);
        tbx_secondsNext.setText(String.valueOf(settings.getInt("secondsToNext", 1)));
        tbx_secondsNext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().equals("")) {
                    if(Integer.parseInt(s.toString()) > 0) {
                        SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("secondsToNext", Integer.parseInt(s.toString()));
                        editor.commit();
                    }
                    else {
                        MainActivity.showAlert(SettingsActivity.this, getString(R.string.error), getString(R.string.settingsNoZero));
                    }
                }
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

    private void visibleTimerOptions(boolean setting) {
        TableRow row1 = (TableRow)findViewById(R.id.row_questionModeTimeReveal);
        TableRow row2 = (TableRow)findViewById(R.id.row_questionModeTimeNext);

        row1.setVisibility( (setting) ? View.VISIBLE : View.GONE );
        row2.setVisibility( (setting) ? View.VISIBLE : View.GONE );
    }
}
