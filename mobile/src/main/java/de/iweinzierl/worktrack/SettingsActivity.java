package de.iweinzierl.worktrack;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.slf4j.Logger;

import de.iweinzierl.worktrack.job.BackupJob;
import de.iweinzierl.worktrack.model.BackupFrequency;
import de.iweinzierl.worktrack.util.SettingsHelper;

@EActivity
public class SettingsActivity extends BaseGoogleApiAvailabilityActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

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
    protected void onStart() {
        super.onStart();
        updateAccounts();
    }

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
                break;
            case SettingsHelper.SETTING_BACKUP_ACCOUNT:
                new SettingsHelper(this).setBackupAccount(prefs.getString(key, null));
                break;
        }
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return Manifest.permission.GET_ACCOUNTS.equals(permission)
                && new SettingsHelper(this).askAgainForGetAccountsPermission();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_GET_ACCOUNT_PERMISSION && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            updateAccounts();
        } else if (requestCode == REQUEST_GET_ACCOUNT_PERMISSION) {
            LOGGER.warn("GET_ACCOUNTS permission not granted!");

            if (shouldShowRequestPermissionRationale(permissions[0])) {
                showRationale(permissions[0]);
            }
        }
    }

    private void showRationale(String permission) {
        if (Manifest.permission.GET_ACCOUNTS.equals(permission)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.util_permission_denied)
                    .setMessage(R.string.util_permission_denied_get_account_rational)
                    .setPositiveButton(R.string.util_permission_ask_again, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_GET_ACCOUNT_PERMISSION);
                        }
                    })
                    .setNegativeButton(R.string.util_permission_dont_ask_again, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new SettingsHelper(SettingsActivity.this).setAskAgainForGetAccountsPermission(false);
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
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

    @Override
    void onConnected() {
    }

    @Override
    void onFailure(int errorCode) {
    }
}
