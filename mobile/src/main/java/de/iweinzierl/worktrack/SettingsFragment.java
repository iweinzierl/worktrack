package de.iweinzierl.worktrack;

import android.accounts.Account;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.PreferenceByKey;

import de.iweinzierl.worktrack.view.settings.LastBackupPreference;

@EFragment
public class SettingsFragment extends PreferenceFragment {

    @PreferenceByKey(R.string.preference_last_backup)
    LastBackupPreference lastBackupPreference;

    @PreferenceByKey(R.string.preference_backup_account)
    ListPreference accountPreference;

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

    public void setAccounts(Account[] accounts) {
        CharSequence[] values = new CharSequence[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            values[i] = accounts[i].name;
        }

        accountPreference.setEntries(values);
        accountPreference.setEntryValues(values);
    }
}
