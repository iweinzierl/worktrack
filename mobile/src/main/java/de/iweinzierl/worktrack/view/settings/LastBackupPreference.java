package de.iweinzierl.worktrack.view.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.iweinzierl.android.utils.UiUtils;

import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import de.iweinzierl.worktrack.R;

public class LastBackupPreference extends Preference {

    private static final NumberFormat NUMBER_FORMAT_SIZE = new DecimalFormat();

    static {
        NUMBER_FORMAT_SIZE.setMaximumFractionDigits(2);
        NUMBER_FORMAT_SIZE.setMinimumFractionDigits(2);
    }

    public LastBackupPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public LastBackupPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LastBackupPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LastBackupPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.preference_last_back, parent, false);

        TextView backupDateView = UiUtils.getGeneric(TextView.class, view, R.id.backup_date_value);
        TextView backupSizeView = UiUtils.getGeneric(TextView.class, view, R.id.backup_size_value);
        TextView backupTrackingItems = UiUtils.getGeneric(TextView.class, view, R.id.backup_tracking_items_value);
        TextView backupWorkplaces = UiUtils.getGeneric(TextView.class, view, R.id.backup_workplaces_value);

        backupSizeView.setText(NUMBER_FORMAT_SIZE.format(getLastBackupSize() / 1000d) + " kB");
        backupDateView.setText(getLastBackupTime());
        backupTrackingItems.setText(String.valueOf(getLastBackupItems()));
        backupWorkplaces.setText(String.valueOf(getLastBackupWorkplaces()));

        return view;
    }


    private String getLastBackupTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        long lastBackupTime = prefs.getLong(LastBackupPreferences.DATE.getProperty(), 0);

        return lastBackupTime > 0
                ? new DateTime(lastBackupTime).toString("yyyy-MM-dd  HH:mm")
                : "unknown";
    }

    private long getLastBackupSize() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getLong(LastBackupPreferences.SIZE.getProperty(), 0);
    }

    private int getLastBackupItems() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getInt(LastBackupPreferences.ITEMS.getProperty(), 0);
    }

    private int getLastBackupWorkplaces() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getInt(LastBackupPreferences.WORKPLACES.getProperty(), 0);
    }
}
