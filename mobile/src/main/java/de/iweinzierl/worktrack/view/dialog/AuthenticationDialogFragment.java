package de.iweinzierl.worktrack.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.github.iweinzierl.android.utils.UiUtils;

import org.slf4j.Logger;

import de.iweinzierl.worktrack.FingerprintHelper;
import de.iweinzierl.worktrack.R;

public class AuthenticationDialogFragment extends DialogFragment {

    private Button usePasswordButton;
    private Button useFingerprintButton;

    public interface Callback {

        void onAuthenticationSucceeded();

        void onAuthenticationFailed();

        void onAuthenticationCancelled();

    }


    private enum AuthenticationType {
        FINGERPRINT,
        PASSWORD
    }

    private static final Logger LOGGER = AndroidLoggerFactory
            .getInstance()
            .getLogger(AuthenticationDialogFragment.class.getName());

    private FingerprintHelper fingerprintHelper;

    private Callback callback;

    private Resources resources;

    private View fingerprintContainer;
    private ImageView fingerprintImage;

    private View passwordContainer;
    private EditText passwordText;

    private View.OnClickListener enableFingerprint = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            enableContainer(AuthenticationType.FINGERPRINT);
        }
    };

    private View.OnClickListener enablePassword = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            enableContainer(AuthenticationType.PASSWORD);
        }
    };

    private View.OnClickListener cancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDialog().dismiss();

            if (callback != null) {
                callback.onAuthenticationCancelled();
            }
        }
    };

    private View.OnClickListener passwordSubmitClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getPasswordText().getText() != null) {
                String password = getPasswordText().getText().toString();
                // TODO go go....
            }

            Snackbar.make(
                    v,
                    "Password authentication not yet supported!",
                    Snackbar.LENGTH_LONG
            ).show();
        }
    };

    private FingerprintManager.AuthenticationCallback authenticationCallback = new FingerprintManager.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            LOGGER.info("Authentication error -> {}", errString);

            if (resources != null) {
                getFingerprintImage().setImageDrawable(
                        resources.getDrawable(R.drawable.ic_fingerprint_red_32dp)
                );
            }

            if (fingerprintHelper.isListening()) {
                showMessage(getFingerprintContainer(), errString.toString());
            }
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
            LOGGER.info("Authentication help -> {}", helpString);

            if (fingerprintHelper.isListening()) {
                showMessage(getFingerprintContainer(), helpString.toString());
            }
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            LOGGER.info("Authentication succeeded");

            if (callback != null) {
                callback.onAuthenticationSucceeded();
            }
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            LOGGER.info("Authentication failed");

            if (callback != null) {
                callback.onAuthenticationFailed();
            }

            if (resources != null) {
                getFingerprintImage().setImageDrawable(
                        resources.getDrawable(R.drawable.ic_fingerprint_red_32dp)
                );
            }

            if (fingerprintHelper.isListening()) {
                showMessage(getFingerprintContainer(), "Fingerprint authentication failed");
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fingerprintHelper = new FingerprintHelper(
                getContext(),
                getContext().getSystemService(FingerprintManager.class),
                authenticationCallback
        );
    }

    @Override
    public void onPause() {
        super.onPause();

        fingerprintHelper.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (fingerprintContainer.getVisibility() == View.VISIBLE && !fingerprintHelper.isListening()) {
            if (!fingerprintHelper.startListening()) {
                showMessage(getFingerprintContainer(), "Unable to use fingerprint sensor");
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        resources = getResources();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        resources = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_authentication_title)
                .setView(createContentView())
                .create();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public View createContentView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_authentication_container, null, false);

        fingerprintContainer = UiUtils.getGeneric(View.class, view, R.id.fingerprint_container);
        fingerprintImage = UiUtils.getGeneric(ImageView.class, view, R.id.fingerprint);
        passwordContainer = UiUtils.getGeneric(View.class, view, R.id.password_container);
        passwordText = UiUtils.getGeneric(EditText.class, view, R.id.password);

        useFingerprintButton = UiUtils.getGeneric(Button.class, view, R.id.use_fingerprint);
        useFingerprintButton.setOnClickListener(enableFingerprint);

        usePasswordButton = UiUtils.getGeneric(Button.class, view, R.id.use_password);
        usePasswordButton.setOnClickListener(enablePassword);

        Button passwordSubmitButton = UiUtils.getGeneric(Button.class, view, R.id.password_submit);
        passwordSubmitButton.setOnClickListener(passwordSubmitClick);

        Button cancelButton = UiUtils.getGeneric(Button.class, view, R.id.cancel);
        cancelButton.setOnClickListener(cancelClick);

        enableContainer(AuthenticationType.FINGERPRINT);

        return view;
    }

    private void enableContainer(AuthenticationType authenticationType) {
        if (authenticationType == AuthenticationType.FINGERPRINT) {
            getFingerprintContainer().setVisibility(View.VISIBLE);
            getUsePasswordButton().setVisibility(View.VISIBLE);
            getPasswordContainer().setVisibility(View.GONE);
            getUseFingerprintButton().setVisibility(View.GONE);

            if (!fingerprintHelper.startListening()) {
                showMessage(getFingerprintContainer(), "Unable to use fingerprint sensor");
            }
        } else {
            getPasswordContainer().setVisibility(View.VISIBLE);
            getUseFingerprintButton().setVisibility(View.VISIBLE);
            getFingerprintContainer().setVisibility(View.GONE);
            getUsePasswordButton().setVisibility(View.GONE);

            fingerprintHelper.stopListening();
        }
    }

    public View getFingerprintContainer() {
        return fingerprintContainer;
    }

    public ImageView getFingerprintImage() {
        return fingerprintImage;
    }

    public View getPasswordContainer() {
        return passwordContainer;
    }

    public EditText getPasswordText() {
        return passwordText;
    }

    public Button getUsePasswordButton() {
        return usePasswordButton;
    }

    public Button getUseFingerprintButton() {
        return useFingerprintButton;
    }

    private void showMessage(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }
}
