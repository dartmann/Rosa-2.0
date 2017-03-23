package de.davidartmann.android.rosa2.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

import de.davidartmann.android.rosa2.R;

public class SettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private SharedPreferences mSharedPreferences;
    private String mEditDialogPref;
    private String mDelDialogPref;
    private String mArchiveDialogPref;
    private String mUnarchiveDialogPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Switch swCancelEdit = (Switch) findViewById(R.id.activity_settings_switch_confirm_cancel_edit);
        Switch swConfDel = (Switch) findViewById(R.id.activity_settings_switch_confirm_deletion);
        Switch swConfArchiving = (Switch) findViewById(R.id.activity_settings_switch_confirm_archiving);
        Switch swConfUnarchiving = (Switch) findViewById(R.id.activity_settings_switch_confirm_unarchiving);
        swConfDel.setOnCheckedChangeListener(this);
        swConfUnarchiving.setOnCheckedChangeListener(this);
        swCancelEdit.setOnCheckedChangeListener(this);
        swConfArchiving.setOnCheckedChangeListener(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setUpSwitches(swCancelEdit, swConfDel, swConfArchiving, swConfUnarchiving);
    }

    @SuppressLint("CommitPrefEdits")
    private void setUpSwitches(Switch swCnclEdit, Switch swConfDel, Switch swConfArchiving, Switch swConfActivation) {
        mEditDialogPref = getString(R.string.edit_dialog_pref);
        if (!mSharedPreferences.contains(mEditDialogPref)) {
            mSharedPreferences.edit().putBoolean(mEditDialogPref, true).commit();
        }
        swCnclEdit.setChecked(mSharedPreferences.getBoolean(mEditDialogPref, true));

        mDelDialogPref = getString(R.string.delete_dialog_pref);
        if (!mSharedPreferences.contains(mDelDialogPref)) {
            mSharedPreferences.edit().putBoolean(mDelDialogPref, true).commit();
        }
        swConfDel.setChecked(mSharedPreferences.getBoolean(mDelDialogPref, true));

        mArchiveDialogPref = getString(R.string.archive_dialog_pref);
        if (!mSharedPreferences.contains(mArchiveDialogPref)) {
            mSharedPreferences.edit().putBoolean(mArchiveDialogPref, true).commit();
        }
        swConfArchiving.setChecked(mSharedPreferences.getBoolean(mArchiveDialogPref, true));

        mUnarchiveDialogPref = getString(R.string.unarchive_dialog_pref);
        if (!mSharedPreferences.contains(mUnarchiveDialogPref)) {
            mSharedPreferences.edit().putBoolean(mUnarchiveDialogPref, true).commit();
        }
        swConfActivation.setChecked(mSharedPreferences.getBoolean(mUnarchiveDialogPref, true));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.activity_settings_switch_confirm_cancel_edit:
                if (prefsDirty(mEditDialogPref, b)) {
                    mSharedPreferences.edit().putBoolean(mEditDialogPref, b).apply();
                }
                break;
            case R.id.activity_settings_switch_confirm_deletion:
                if (prefsDirty(mDelDialogPref, b)) {
                    mSharedPreferences.edit().putBoolean(mDelDialogPref, b).apply();
                }
                break;
            case R.id.activity_settings_switch_confirm_archiving:
                if (prefsDirty(mArchiveDialogPref, b)) {
                    mSharedPreferences.edit().putBoolean(mArchiveDialogPref, b).apply();
                }
                break;
            case R.id.activity_settings_switch_confirm_unarchiving:
                if (prefsDirty(mUnarchiveDialogPref, b)) {
                    mSharedPreferences.edit().putBoolean(mUnarchiveDialogPref, b).apply();
                }
                break;
        }
    }

    /**
     * Checks if a (maybe non existent) preference has an other value than the given one.
     * @param prefName name of preference.
     * @param value actual given value.
     * @return true if dirty, false otherwise.
     */
    private boolean prefsDirty(String prefName, boolean value) {
        return mSharedPreferences.getBoolean(prefName, true) != value;
    }
}
