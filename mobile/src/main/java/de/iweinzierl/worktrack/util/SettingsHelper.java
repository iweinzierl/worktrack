package de.iweinzierl.worktrack.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class SettingsHelper {

    public static final String SETTING_DAILY_WORKING_HOURS = "setting_daily_working_hours";
    public static final String SETTING_WEEKLY_WORKING_HOURS = "setting_weekly_working_hours";

    public static final int DEFAULT_DAILY_WORKING_HOURS = 8;
    public static final int DEFAULT_WEEKLY_WORKING_HOURS = 40;

    private final Context context;

    public SettingsHelper(Context context) {
        this.context = context;
    }

    public int getDailyWorkingHours() {
        return getInt(SETTING_DAILY_WORKING_HOURS, DEFAULT_DAILY_WORKING_HOURS);
    }

    public int getWeeklyWorkingHours() {
        return getInt(SETTING_WEEKLY_WORKING_HOURS, DEFAULT_WEEKLY_WORKING_HOURS);

    }

    private int getInt(String key, int defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = prefs.getString(key, null);
        return value == null ? defaultValue : Integer.valueOf(value);
    }
}
