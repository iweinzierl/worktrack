package de.iweinzierl.worktrack;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.PermissionChecker;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.UiThread;
import org.slf4j.Logger;

import de.iweinzierl.worktrack.job.BackupJob;
import de.iweinzierl.worktrack.model.BackupFrequency;
import de.iweinzierl.worktrack.model.BackupMetaData;
import de.iweinzierl.worktrack.util.BackupHelper;

@EActivity
public class SettingsActivity extends BaseGoogleApiActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger(SettingsActivity.class.getName());

    public static final String ACCOUNT_TYPE_GOOGLE = "com.google";

    private static final int REQUEST_GET_ACCOUNT_PERMISSION = 1001;

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

        updateAccounts();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_GET_ACCOUNT_PERMISSION && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            updateAccounts();
        } else {
            LOGGER.warn("GET_ACCOUNTS permission not granted!");
        }
    }

    protected void updateAccounts() {
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_GET_ACCOUNT_PERMISSION);
            return;
        }

        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE_GOOGLE);

        settingsFragment.setAccounts(accounts);
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
