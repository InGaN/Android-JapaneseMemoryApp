package com.example.kevin.japanesememoryapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
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

import java.util.concurrent.Callable;

public class SettingsActivity extends AppCompatActivity {
    public static final String PREFERENCES_FILE_NAME = "MyPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        applyTheme(settings);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);


        TextView lbl_about = (TextView)findViewById(R.id.lbl_settingsAbout);
        lbl_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.showAlert(SettingsActivity.this, getString(R.string.aboutTitle), getString(R.string.aboutMessage) + "\r" + getString(R.string.aboutWebsite));
            }
        });

        initializeItems(settings);
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
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_settings, menu);
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

    private void setSpinners(final SharedPreferences settings) {
        Spinner spn_questionMode = (Spinner)findViewById(R.id.spn_questionMode);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.questionModes, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_questionMode.setAdapter(adapter1);

        spn_questionMode.setSelection((int) settings.getLong("questionMode", 0L));
        spn_questionMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerChanged((int) id, "questionMode", settings, new VisibleTimerOptions(), (id == 0));
                /*SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putLong("questionMode", id);
                editor.commit();
                visibleTimerOptions(id == 0); */
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner spn_themes = (Spinner)findViewById(R.id.spn_themeSets);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.selectableThemes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_themes.setAdapter(adapter);

        spn_themes.setSelection((int) settings.getLong("themeSelection", 0L));
        spn_themes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerChanged((int) id, "themeSelection", settings, null, false);
                /*SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putLong("themeSelection", id);
                editor.commit();*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner spn_inputMode = (Spinner)findViewById(R.id.spn_inputMode);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.inputModes, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_inputMode.setAdapter(adapter2);

        spn_inputMode.setSelection((int) settings.getLong("inputMode", 0L));
        spn_inputMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerChanged((int) id, "inputMode", settings, new VisibleInputTypeOptions(), (id == 0));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner spn_inputModeType = (Spinner)findViewById(R.id.spn_inputModeType);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.inputModeTypes, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_inputModeType.setAdapter(adapter3);

        spn_inputModeType.setSelection((int) settings.getLong("inputModeType", 0L));
        spn_inputModeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerChanged((int) id, "inputModeType", settings, null, false);
                /*SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putLong("inputModeType", id);
                editor.commit();*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void spinnerChanged(int input, String preference, SharedPreferences settings, SetVisibles visibles, boolean visibility) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(preference, input);
        editor.commit();
        if(visibles != null)
            visibles.execute(visibility);
    }

    private interface SetVisibles {
        public void execute(boolean setting);
    }

    private class VisibleTimerOptions implements SetVisibles {
        @Override
        public void execute(boolean setting) {
            TableRow row1 = (TableRow)findViewById(R.id.row_questionModeTimeReveal);
            TableRow row2 = (TableRow)findViewById(R.id.row_questionModeTimeNext);
            row1.setVisibility( (setting) ? View.VISIBLE : View.GONE );
            row2.setVisibility( (setting) ? View.VISIBLE : View.GONE );
        }
    }

    private class VisibleInputTypeOptions implements SetVisibles {
        @Override
        public void execute(boolean setting) {
            TableRow row = (TableRow)findViewById(R.id.row_inputModeTypeOptions);
            row.setVisibility((setting) ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setEditTexts(SharedPreferences settings) {
        EditText tbx_secondsReveal = (EditText)findViewById(R.id.tbx_timerReveal);
        initializeEditText(tbx_secondsReveal, "secondsToReveal", settings, 6);

        EditText tbx_secondsNext = (EditText)findViewById(R.id.tbx_timerNext);
        initializeEditText(tbx_secondsNext, "secondsToNext", settings, 3);
    }

    private void initializeEditText(EditText box, final String preference, SharedPreferences settings, int defaultValue) {
        box.setText(String.valueOf(settings.getInt(preference, defaultValue)));
        box.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals("")) {
                    editTextChanged(Integer.parseInt(s.toString()), preference);
                }
            }
        });
    }

    private void editTextChanged(int input, String preference) {
        if(input > 0) {
            SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(preference, input);
            editor.commit();
        }
        else {
            MainActivity.showAlert(SettingsActivity.this, getString(R.string.error), getString(R.string.settingsNoZero));
        }
    }

    private void setSwitches(final SharedPreferences settings) {
        Switch swc_furigana = (Switch)findViewById(R.id.swc_furigana);
        initializeSwitch(swc_furigana, "furiganaActive", settings);

        Switch swc_kanji = (Switch)findViewById(R.id.swc_kanji);
        initializeSwitch(swc_kanji, "kanjiActive", settings);

        Switch swc_meaning = (Switch)findViewById(R.id.swc_meaning);
        initializeSwitch(swc_meaning, "meaningActive", settings);

        Switch swc_difficulty = (Switch)findViewById(R.id.swc_difficulty);
        initializeSwitch(swc_difficulty, "difficultyActive", settings);

        Switch swc_showMenuButtons = (Switch)findViewById(R.id.swc_showMenuButtons);
        initializeSwitch(swc_showMenuButtons, "showMenuButtons", settings);
    }

    private void initializeSwitch(Switch sw, final String preference, final SharedPreferences settings) {
        sw.setChecked(settings.getBoolean(preference, true));
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(preference, isChecked);
                editor.commit();
            }
        });
    }
}
