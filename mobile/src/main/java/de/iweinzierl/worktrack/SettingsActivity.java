package de.iweinzierl.worktrack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.UiThread;

import de.iweinzierl.worktrack.job.BackupJob;
import de.iweinzierl.worktrack.model.BackupFrequency;
import de.iweinzierl.worktrack.model.BackupMetaData;
import de.iweinzierl.worktrack.util.BackupHelper;

@EActivity
public class SettingsActivity extends BaseGoogleApiActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    int getLayoutId() {
        return R.layout.activity_settings;
    }

    @FragmentById(R.id.settingsFragment)
    SettingsFragment settingsFragment;

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
    public void onConnected(@Nullable Bundle bundle) {
        super.onConnected(bundle);
        updateLastBackupSize();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        switch (key) {
            case "settings_backup_frequency":
                backupFrequencyChanged(prefs.getString(key, null));
        }
    }

    @Background
    protected void updateLastBackupSize() {
        new BackupHelper(new BackupHelper.DefaultBackupCallback() {
            @Override
            public void onGetLastBackup(BackupMetaData lastBackup) {
                updateLastBackupSize(lastBackup.getSize());
            }
        }, trackingItemRepository, getGoogleApiClient()
        ).getLastBackup();
    }

    @UiThread
    protected void updateLastBackupSize(long size) {
        settingsFragment.setLastBackupSize(size / 1024f);
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
