package de.iweinzierl.worktrack.view.settings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.iweinzierl.worktrack.R;

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

        return view;
    }
}
