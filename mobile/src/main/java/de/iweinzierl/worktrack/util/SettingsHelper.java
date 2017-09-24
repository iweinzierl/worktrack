package de.iweinzierl.worktrack.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class SettingsHelper {

    public static final String SETTING_DAILY_WORKING_HOURS = "setting_daily_working_hours";
    public static final String SETTING_WEEKLY_WORKING_HOURS = "setting_weekly_working_hours";
    public static final String SETTING_DEFAULT_MAIL = "setting_exports_default_mail";
    public static final String SETTING_BACKUP_ACCOUNT = "preference_backup_account";

    public static final String SETTING_PERMISSIONS_GET_ACCOUNTS_ASK_AGAIN = "settings.permissions.getaccounts.askagain";

    public static final int DEFAULT_DAILY_WORKING_HOURS = 8;
    public static final int DEFAULT_WEEKLY_WORKING_HOURS = 40;

    private final Context context;

    public SettingsHelper(Context context) {
        this.context = context;
    }

    public String getBackupAccount() {
        return getString(SETTING_BACKUP_ACCOUNT, null);
    }

    public void setBackupAccount(String account) {
        setString(SETTING_BACKUP_ACCOUNT, account);
    }

    public int getDailyWorkingHours() {
        return getInt(SETTING_DAILY_WORKING_HOURS, DEFAULT_DAILY_WORKING_HOURS);
    }

    public int getWeeklyWorkingHours() {
        return getInt(SETTING_WEEKLY_WORKING_HOURS, DEFAULT_WEEKLY_WORKING_HOURS);

    }

    public boolean askAgainForGetAccountsPermission() {
        return getBoolean(SETTING_PERMISSIONS_GET_ACCOUNTS_ASK_AGAIN, true);
    }

    public void setAskAgainForGetAccountsPermission(boolean askAgain) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(SETTING_PERMISSIONS_GET_ACCOUNTS_ASK_AGAIN, askAgain).apply();
    }

    public String getDefaultEmailAddress() {
        return getString(SETTING_DEFAULT_MAIL, null);
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(key, defaultValue);
    }

    private String getString(String key, String defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    private void setString(String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(key, value).apply();
    }

    private int getInt(String key, int defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = prefs.getString(key, null);
        return value == null ? defaultValue : Integer.valueOf(value);
    }
}
