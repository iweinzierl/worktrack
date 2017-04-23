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

import static de.iweinzierl.worktrack.job.BackupJob.PREFS_LAST_BACKUP;

public class LastBackupPreference extends Preference {

    private static final NumberFormat NUMBER_FORMAT_SIZE = new DecimalFormat();

    static {
        NUMBER_FORMAT_SIZE.setMaximumFractionDigits(2);
        NUMBER_FORMAT_SIZE.setMinimumFractionDigits(2);
    }

    private TextView backupDateView;
    private TextView backupSizeView;

    private float lastBackupSizeKb;

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

        backupDateView = UiUtils.getGeneric(TextView.class, view, R.id.backup_date_value);
        backupSizeView = UiUtils.getGeneric(TextView.class, view, R.id.backup_size_value);


        backupDateView.setText(getLastBackupTime());
        updateLastBackupSize();

        return view;
    }

    private String getLastBackupTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        long lastBackupTime = prefs.getLong(PREFS_LAST_BACKUP, 0);

        return lastBackupTime > 0
                ? new DateTime(lastBackupTime).toString("yyyy-MM-dd  HH:mm")
                : "unknown";
    }

    public void setLastBackupSize(float kiloBytes) {
        this.lastBackupSizeKb = kiloBytes;
        updateLastBackupSize();
    }

    private void updateLastBackupSize() {
        if (lastBackupSizeKb >= 0 && backupSizeView != null) {
            String size = NUMBER_FORMAT_SIZE.format(lastBackupSizeKb) + " kB";
            backupSizeView.setText(size);
        }
    }
}
