package de.iweinzierl.worktrack;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.PreferenceByKey;

import de.iweinzierl.worktrack.view.settings.LastBackupPreference;

@EFragment
public class SettingsFragment extends PreferenceFragment {

    @PreferenceByKey(R.string.preference_last_backup)
    LastBackupPreference lastBackupPreference;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    public void setLastBackupSize(float kiloBytes) {
        lastBackupPreference.setLastBackupSize(kiloBytes);
    }
}
