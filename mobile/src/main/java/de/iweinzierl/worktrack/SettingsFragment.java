package de.iweinzierl.worktrack;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.androidannotations.annotations.EFragment;

@EFragment
public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
