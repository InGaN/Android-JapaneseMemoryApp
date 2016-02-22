package com.example.kevin.japanesememoryapp;

import android.app.Dialog;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.concurrent.Callable;

public class SettingsActivity extends AppCompatActivity {
    public static final String PREFERENCES_FILE_NAME = "MyPreferences";
    public static final int SECONDS_MAX_UNTIL_REVEAL = 30;
    public static final int SECONDS_MAX_UNTIL_NEXT = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        applyTheme(settings);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeItems(SharedPreferences settings) {
        setClickables(settings);
        setSpinners(settings);
        setSwitches(settings);

        TextView lbl_seconds_reveal = (TextView)findViewById(R.id.lbl_secondsReveal);
        lbl_seconds_reveal.setText(getString(R.string.settingsTimerModeSecondsReveal) + " " + settings.getInt("secondsToReveal", SECONDS_MAX_UNTIL_REVEAL));
        TextView lbl_seconds_next = (TextView)findViewById(R.id.lbl_secondsNext);
        lbl_seconds_next.setText(getString(R.string.settingsTimerModeSecondsNext) + " " + settings.getInt("secondsToNext", SECONDS_MAX_UNTIL_NEXT));
    }

    private void setClickables(final SharedPreferences settings) {
        TableRow row_setFontSizes = (TableRow)findViewById(R.id.row_setFontSizes);
        row_setFontSizes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSizes();
            }
        });

        TextView lbl_about = (TextView)findViewById(R.id.lbl_settingsAbout);
        lbl_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.showAlert(SettingsActivity.this, getString(R.string.aboutTitle), getString(R.string.aboutMessage) + "\r" + getString(R.string.aboutWebsite));
            }
        });

        TableRow row_questionModeTimeReveal = (TableRow)findViewById(R.id.row_questionModeTimeReveal);
        row_questionModeTimeReveal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumericDialog(
                        (TextView) findViewById(R.id.lbl_secondsReveal),
                        getString(R.string.settingsTimerModeSecondsReveal),
                        "secondsToReveal",
                        settings,
                        SECONDS_MAX_UNTIL_REVEAL);
            }
        });
        TableRow row_questionModeTimeNext = (TableRow)findViewById(R.id.row_questionModeTimeNext);
        row_questionModeTimeNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumericDialog(
                        (TextView)findViewById(R.id.lbl_secondsNext),
                        getString(R.string.settingsTimerModeSecondsNext),
                        "secondsToNext",
                        settings,
                        SECONDS_MAX_UNTIL_NEXT);
            }
        });
    }

    private void openNumericDialog(final TextView label, final String labelText, final String preference, final SharedPreferences settings, final int maximum) {
        final Dialog dialog = new Dialog(SettingsActivity.this);
        dialog.setTitle(labelText);
        dialog.setContentView(R.layout.activity_dialog_number_picker);

        final NumberPicker picker = (NumberPicker)dialog.findViewById(R.id.nmp_picker);
        picker.setMaxValue(maximum);
        picker.setMinValue(1);
        picker.setValue(settings.getInt(preference, maximum));

        Button btn_apply = (Button)dialog.findViewById(R.id.btn_apply);
        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(preference, picker.getValue());
                editor.commit();
                label.setText(labelText + " " + settings.getInt(preference, maximum));
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void editSizes() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("incoming_size", true);
        startActivity(intent);
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
