package de.iweinzierl.worktrack.job;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import org.joda.time.DateTime;
import org.slf4j.Logger;

import de.iweinzierl.worktrack.model.BackupFrequency;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository_;
import de.iweinzierl.worktrack.util.BackupHelper;

public class BackupJob extends JobService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger(BackupJob.class.getName());

    public static final int JOB_ID = 901;

    public static final String PREFS_LAST_BACKUP = "backup.job.last.backup";
    public static final String PREFS_BACKUP_FREQUENCY = "settings_backup_frequency";

    public static final long INTERVAL_DAILY = 1000L * 60 * 60 * 24;

    private GoogleApiClient googleApiClient;
    private JobParameters jobParameters;

    @Override
    public boolean onStartJob(JobParameters params) {
        jobParameters = params;
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addApi(Drive.API)
                .build();

        googleApiClient.connect();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        LOGGER.info("Backup job stopped");
        return false;
    }


    public static void stopBackupJob(Context context) {
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        scheduler.cancel(BackupJob.JOB_ID);

        LOGGER.info("Cancelled backup job");
    }

    public static void scheduleBackups(Context context, BackupFrequency frequency) {
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        JobInfo.Builder builder = new JobInfo.Builder(BackupJob.JOB_ID, new ComponentName(context, BackupJob.class))
                .setPersisted(true)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false);

        switch (frequency) {
            case DAILY:
                builder.setPeriodic(BackupJob.INTERVAL_DAILY);
                break;
            case WEEKLY:
                builder.setPeriodic(BackupJob.INTERVAL_DAILY);
                break;
            case MONTHLY:
                builder.setPeriodic(BackupJob.INTERVAL_DAILY);
                break;
        }

        if (scheduler.schedule(builder.build()) == JobScheduler.RESULT_SUCCESS) {
            LOGGER.info("Scheduled backup job successfully");
        } else {
            LOGGER.error("Unable to schedule backup job");
        }
    }

    public DateTime getLastBackupTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long lastBackupTime = prefs.getLong(PREFS_LAST_BACKUP, 0);

        return lastBackupTime > 0
                ? new DateTime(lastBackupTime)
                : null;
    }

    public BackupFrequency getBackupFrequency() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backupFrequency = prefs.getString(PREFS_BACKUP_FREQUENCY, null);

        if (backupFrequency != null) {
            return BackupFrequency.valueOf(backupFrequency);
        } else {
            LOGGER.warn("BackupJob running but no frequency found in preferences!");
            return BackupFrequency.NEVER;
        }
    }

    private boolean backup(JobParameters params) {
        LOGGER.info("Backup job started");

        if (!new BackupDecisionMaker(getBackupFrequency(), getLastBackupTime()).backupRequired()) {
            LOGGER.debug("Job not required to run.");
            jobFinished(params, false);

            return false;
        }

        try {
            doBackup();
            jobFinished(params, false);
        } catch (Exception e) {
            LOGGER.error("Error while running BackupJob.", e);
            jobFinished(params, true);
        }

        return false;
    }

    private void doBackup() throws Exception {
        LOGGER.info("doBackup()");

        LocalTrackingItemRepository repository =
                LocalTrackingItemRepository_.getInstance_(getApplicationContext());

        new BackupHelper(new BackupHelper.DefaultBackupCallback() {
            @Override
            public void onCreationSuccessful() {
                updateLastBackupTime();
            }
        }, repository, googleApiClient).writeBackup("automatic-backup.csv");

        LOGGER.info("Successfully finished backupJob.");
    }

    private void updateLastBackupTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putLong(PREFS_LAST_BACKUP, DateTime.now().getMillis())
                .apply();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        backup(jobParameters);
    }

    @Override
    public void onConnectionSuspended(int i) {
        jobFinished(jobParameters, false);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        jobFinished(jobParameters, true);
    }
}
