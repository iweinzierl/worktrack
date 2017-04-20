package de.iweinzierl.worktrack.view.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.iweinzierl.android.utils.UiUtils;

import org.joda.time.DateTime;

import de.iweinzierl.worktrack.R;

import static de.iweinzierl.worktrack.job.BackupJob.PREFS_LAST_BACKUP;

public class LastBackupPreference extends Preference {

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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.preference_last_back, parent, false);

        UiUtils.setSafeText(view, R.id.backup_date_value, getLastBackupTime());

        return view;
    }

    private String getLastBackupTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        long lastBackupTime = prefs.getLong(PREFS_LAST_BACKUP, 0);

        return lastBackupTime > 0
                ? new DateTime(lastBackupTime).toString("yyyy-MM-dd  HH:mm")
                : "unknown";
    }
}
