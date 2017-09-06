package de.iweinzierl.worktrack.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.TextView;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.persistence.Workplace;

public class WorkplaceTitleQueryDialog {

    public interface Callback {
        void onSubmit(Workplace workplace);

        void onCancel(Workplace workplace);
    }

    private final Workplace workplace;
    private final Context context;
    private final Callback callback;

    private EditText nameView;

    public WorkplaceTitleQueryDialog(Workplace workplace, Context context, Callback callback) {
        this.workplace = workplace;
        this.context = context;
        this.callback = callback;
    }

    public void show() {
        Dialog dialog = new AlertDialog.Builder(context)
                .setView(R.layout.dialog_workplace_title_query_content)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_workplace_title_query_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        fireOnSubmit();
                    }
                })
                .setNegativeButton(R.string.dialog_workplace_title_query_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        fireOnCancel();
                    }
                })
                .show();

        ((TextView) dialog.findViewById(R.id.location)).setText(createLocationString());
        ((TextView) dialog.findViewById(R.id.radius)).setText(String.valueOf(workplace.getRadius()));
        nameView = (EditText) dialog.findViewById(R.id.name);
    }

    private String createLocationString() {
        double lat = ((int) (workplace.getLat() * 1000d)) / 1000d;
        double lon = ((int) (workplace.getLon() * 1000d)) / 1000d;

        return String.valueOf(lat + " / " + lon);
    }

    private void fireOnCancel() {
        if (callback != null) {
            callback.onCancel(workplace);
        }
    }

    private void fireOnSubmit() {
        if (callback != null) {
            workplace.setName(nameView.getText().toString());
            callback.onSubmit(workplace);
        }
    }
}
