package de.iweinzierl.worktrack;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import org.androidannotations.annotations.EActivity;
import org.slf4j.Logger;

@EActivity
public abstract class BaseGoogleApiActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_RESOLUTION = 1;

    private static final String PREFERENCES_BACKUP_ACCOUNT = "preference_backup_account";

    private static final Logger LOG = AndroidLoggerFactory.getInstance().getLogger(BaseGoogleApiActivity.class.getName());

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googleApiClient = new GoogleApiClient.Builder(this)
                .setAccountName(getAccount())
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LOG.info("Successfully connected to Google API.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        LOG.info("Connection to Google API suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LOG.error("Connection to Google API failed -> {} -> {}",
                connectionResult.getErrorMessage(),
                connectionResult.getErrorCode());

        if (!connectionResult.hasResolution()) {
            LOG.warn("Unable to resolve Google API connection error.");
            GoogleApiAvailability.getInstance().getErrorDialog(
                    this, connectionResult.getErrorCode(), 0).show();
        } else {
            try {
                connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                LOG.error("Unable to resolve connection issue to Google APIs.", e);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LOG.info("ActivityResult received: {} -> {}", requestCode, resultCode);

        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                LOG.info("ActivityResult for -> resolution");
                if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
                    googleApiClient.connect();
                }
        }
    }

    protected GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    private String getAccount() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString(PREFERENCES_BACKUP_ACCOUNT, null);
    }
}
