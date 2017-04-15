package de.iweinzierl.worktrack;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHelper {

    private final Context context;
    private final FingerprintManager fingerprintManager;
    private final FingerprintManager.AuthenticationCallback authenticationCallback;
    private CancellationSignal cancellationSignal;

    public FingerprintHelper(Context context, FingerprintManager fingerprintManager, FingerprintManager.AuthenticationCallback authenticationCallback) {
        this.context = context;
        this.fingerprintManager = fingerprintManager;
        this.authenticationCallback = authenticationCallback;
    }

    public boolean isFingerprintAvailable() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return fingerprintManager.isHardwareDetected()
                && fingerprintManager.hasEnrolledFingerprints();
    }

    public boolean isListening() {
        return cancellationSignal != null;
    }

    public boolean startListening() {
        cancellationSignal = new CancellationSignal();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        fingerprintManager.authenticate(
                null,
                cancellationSignal,
                0,
                authenticationCallback,
                null
        );

        return true;
    }

    public void stopListening() {
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }
}
