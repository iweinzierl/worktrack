package de.iweinzierl.worktrack.view;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import de.iweinzierl.worktrack.R;

public class DateTimeChooserDialogBuilder {

    private final Context context;

    public DateTimeChooserDialogBuilder(Context context) {
        this.context = context;
    }

    public AlertDialog build() {
        return new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_datetime_chooser_title)
                .setView(R.layout.dialog_datetime_chooser)
                .create();
    }
}
