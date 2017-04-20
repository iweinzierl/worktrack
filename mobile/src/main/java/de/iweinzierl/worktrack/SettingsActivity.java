package de.iweinzierl.worktrack;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import org.androidannotations.annotations.EActivity;

import de.iweinzierl.worktrack.job.BackupJob;
import de.iweinzierl.worktrack.model.BackupFrequency;

@EActivity(R.layout.activity_settings)
public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        switch (key) {
            case "settings_backup_frequency":
                backupFrequencyChanged(prefs.getString(key, null));
        }
    }

    private void backupFrequencyChanged(String newValue) {
        BackupFrequency frequency = BackupFrequency.valueOf(newValue);

        switch (frequency) {
            case NEVER:
                BackupJob.stopBackupJob(this);
                return;
            default:
                BackupJob.scheduleBackups(this, frequency);
        }
    }
}
