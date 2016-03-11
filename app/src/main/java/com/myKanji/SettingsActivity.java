package com.myKanji;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    public static final String PREFERENCES_FILE_NAME = "MyPreferences";
    public static final int SECONDS_MAX_UNTIL_REVEAL = 30;
    public static final int SECONDS_MAX_UNTIL_NEXT = 30;
    public static final int DEFAULT_DIFFICULTY = 5;

    public static final int MODE_TIMED = 0;
    public static final int MODE_BUTTON = 1;
    public static final int INPUT_TYPE = 0;
    public static final int INPUT_YESNO = 1;
    public static final int INPUT_NONE = 2;
    public static final int INPUT_TYPE_FURIGANA = 0;
    public static final int INPUT_TYPE_KANJI = 1;
    public static final int INPUT_TYPE_MEANING = 2;
    public static final int SORT_RANDOM = 0;
    public static final int SORT_DIFFICULTY_ASC = 1;
    public static final int SORT_DIFFICULTY_DESC = 2;
    public static final int SORT_ID_ASC = 3;
    public static final int SORT_ID_DESC = 4;

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
        setCheckboxes(settings);

        new VisibleFilters().execute(settings.getBoolean("filter", false));

        TextView lbl_seconds_reveal = (TextView)findViewById(R.id.lbl_secondsReveal);
        lbl_seconds_reveal.setText(getString(R.string.settingsTimerModeSecondsReveal) + " " + settings.getInt("secondsToReveal", SECONDS_MAX_UNTIL_REVEAL));
        TextView lbl_seconds_next = (TextView)findViewById(R.id.lbl_secondsNext);
        lbl_seconds_next.setText(getString(R.string.settingsTimerModeSecondsNext) + " " + settings.getInt("secondsToNext", SECONDS_MAX_UNTIL_NEXT));
        TextView lbl_defaultDifficulty = (TextView)findViewById(R.id.lbl_settingsDefaultDifficulty);
        lbl_defaultDifficulty.setText(getString(R.string.settingsDefaultDifficulty) + " " + settings.getInt("defaultDifficulty", DEFAULT_DIFFICULTY));
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
        TableRow row_defaultDifficulty = (TableRow)findViewById(R.id.row_defaultDifficulty);
        row_defaultDifficulty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumericDialog(
                        (TextView)findViewById(R.id.lbl_settingsDefaultDifficulty),
                        getString(R.string.settingsDefaultDifficulty),
                        "defaultDifficulty",
                        settings,
                        9);
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

        Spinner spn_sortOrder = (Spinner)findViewById(R.id.spn_sortOrder);
        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(this, R.array.sortOrder, android.R.layout.simple_spinner_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_sortOrder.setAdapter(adapter4);

        spn_sortOrder.setSelection((int) settings.getLong("sortOrder", 0L));
        spn_sortOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerChanged((int) id, "sortOrder", settings, null, false);
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
    private class VisibleFilters implements SetVisibles {
        @Override
        public void execute(boolean setting) {
            TableLayout con_filters = (TableLayout)findViewById(R.id.con_filters);
            con_filters.setVisibility((setting) ? View.VISIBLE : View.GONE);
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
        Switch swc_allowDifficulty = (Switch)findViewById(R.id.swc_changeDifficulty);
        initializeSwitch(swc_allowDifficulty, "allowDifficultyChange", settings, null);

        Switch swc_furigana = (Switch)findViewById(R.id.swc_furigana);
        initializeSwitch(swc_furigana, "furiganaActive", settings, null);

        Switch swc_kanji = (Switch)findViewById(R.id.swc_kanji);
        initializeSwitch(swc_kanji, "kanjiActive", settings, null);

        Switch swc_meaning = (Switch)findViewById(R.id.swc_meaning);
        initializeSwitch(swc_meaning, "meaningActive", settings, null);

        Switch swc_difficulty = (Switch)findViewById(R.id.swc_difficulty);
        initializeSwitch(swc_difficulty, "difficultyActive", settings, null);

        Switch swc_showMenuButtons = (Switch)findViewById(R.id.swc_showMenuButtons);
        initializeSwitch(swc_showMenuButtons, "showMenuButtons", settings, null);

        Switch swc_filter = (Switch)findViewById(R.id.swc_filter);
        initializeSwitch(swc_filter, "filter", settings, new VisibleFilters());
    }

    private void initializeSwitch(Switch sw, final String preference, final SharedPreferences settings, final SetVisibles visibles) {
        sw.setChecked(settings.getBoolean(preference, true));
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(preference, isChecked);
                editor.commit();
                if (visibles != null)
                    visibles.execute(isChecked);
            }
        });
    }

    private void setCheckboxes(final SharedPreferences settings) {
        initializeCheckbox((CheckBox)findViewById(R.id.cbx_1), "filter1", settings);
        initializeCheckbox((CheckBox)findViewById(R.id.cbx_2), "filter2", settings);
        initializeCheckbox((CheckBox)findViewById(R.id.cbx_3), "filter3", settings);
        initializeCheckbox((CheckBox)findViewById(R.id.cbx_4), "filter4", settings);
        initializeCheckbox((CheckBox)findViewById(R.id.cbx_5), "filter5", settings);
        initializeCheckbox((CheckBox)findViewById(R.id.cbx_6), "filter6", settings);
        initializeCheckbox((CheckBox)findViewById(R.id.cbx_7), "filter7", settings);
        initializeCheckbox((CheckBox)findViewById(R.id.cbx_8), "filter8", settings);
        initializeCheckbox((CheckBox)findViewById(R.id.cbx_9), "filter9", settings);
    }

    private void initializeCheckbox(CheckBox cbx, final String preference, final SharedPreferences settings) {
        cbx.setChecked(settings.getBoolean(preference, true));
        cbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(preference, isChecked);
                editor.commit();
            }
        });
    }
}
