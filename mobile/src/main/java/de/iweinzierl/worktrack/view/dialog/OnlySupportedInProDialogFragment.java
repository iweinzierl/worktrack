package de.iweinzierl.worktrack.view.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.iweinzierl.android.utils.UiUtils;

import de.iweinzierl.worktrack.R;
import de.iweinzierl.worktrack.WorktrackApplication;

public class OnlySupportedInProDialogFragment extends DialogFragment {

    int titleResId;
    int messageResId;

    View content;
    TextView titleView;
    TextView messageView;

    public static OnlySupportedInProDialogFragment newInstance() {
        return new OnlySupportedInProDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        content = inflater.inflate(R.layout.dialog_feature_only_in_pro, null, false);

        titleView = UiUtils.getGeneric(TextView.class, content, R.id.title);
        titleView.setText(titleResId);

        messageView = UiUtils.getGeneric(TextView.class, content, R.id.message);
        messageView.setText(messageResId);

        Button positiveButton = UiUtils.getGeneric(Button.class, content, R.id.positive);
        Button negativeButton = UiUtils.getGeneric(Button.class, content, R.id.negative);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + WorktrackApplication.PACKAGE_NAME_PRO));
                getContext().startActivity(intent);
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return new AlertDialog.Builder(getContext())
                .setView(content)
                .create();
    }

    public void setTitleResId(int titleResId) {
        this.titleResId = titleResId;
    }

    public void setMessageResId(int messageResId) {
        this.messageResId = messageResId;
    }
}
