package de.iweinzierl.worktrack;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;

import java.util.Arrays;

@EActivity
public abstract class BaseGoogleApiAvailabilityActivity extends BaseActivity {

    interface Action {
        void execute();
    }

    private static final String[] SCOPES = {DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_METADATA};

    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    static final int ERROR_UNDEFINED_ACCOUNT = 1000;
    static final int ERROR_AUTHENTICATION = 2000;

    private GoogleAccountCredential googleAccountCredential;

    abstract void onConnected();

    abstract void onFailure(int errorCode);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String account = prefs.getString(getString(R.string.preference_backup_account), null);

        googleAccountCredential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        googleAccountCredential.setSelectedAccountName(account);
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    onFailure(ERROR_AUTHENTICATION);
                } else {
                    onConnected();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    onConnected();
                }
                break;
        }
    }

    protected GoogleAccountCredential getCredential() {
        return googleAccountCredential;
    }

    @Background
    protected void executeAction(Action action) {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (googleAccountCredential.getSelectedAccountName() == null) {
            onFailure(ERROR_UNDEFINED_ACCOUNT);
        } else {
            action.execute();
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int status = availability.isGooglePlayServicesAvailable(this);

        return status == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = availability.isGooglePlayServicesAvailable(this);

        if (availability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    private void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                BaseGoogleApiAvailabilityActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
}
