package de.iweinzierl.worktrack.util;


import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class LaunchHelper {

    private static final String PREFIX_FIRST_LAUNCH = "launch.first.";

    public static boolean isFirstLaunch(Activity activity) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        boolean first = preferences
                .getBoolean(PREFIX_FIRST_LAUNCH + activity.getLocalClassName(), true);

        preferences.edit()
                .putBoolean(PREFIX_FIRST_LAUNCH + activity.getLocalClassName(), false)
                .apply();

        return first;
    }
}
