package de.iweinzierl.worktrack.view.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.github.iweinzierl.android.utils.UiUtils;

import org.joda.time.LocalDate;

import de.iweinzierl.worktrack.R;

public class BackupTitleInputDialog extends DialogFragment {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public interface Callback {
        void onConfirm(String title);

        void onCancel();
    }

    protected EditText titleInput;
    protected Callback callback;

    protected DialogInterface.OnClickListener confirmListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (callback == null) {
                throw new IllegalArgumentException("No callback provided");
            }

            if (titleInput.getText() != null) {
                callback.onConfirm(titleInput.getText().toString());
            }
        }
    };

    protected DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();

            if (callback == null) {
                throw new IllegalArgumentException("No callback provided");
            }

            callback.onCancel();
        }
    };

    public View createContentView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View contentView = inflater.inflate(R.layout.dialog_backup_title_input, null, false);
        titleInput = UiUtils.getGeneric(EditText.class, contentView, R.id.title);
        titleInput.setText(LocalDate.now().toString(DATE_FORMAT));

        return contentView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_backup_title_input_title)
                .setView(createContentView())
                .setPositiveButton(R.string.dialog_backup_title_input_confirm, confirmListener)
                .setNegativeButton(R.string.dialog_backup_title_input_cancel, cancelListener)
                .create();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
