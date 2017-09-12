package de.iweinzierl.worktrack.job;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

import org.joda.time.DateTime;
import org.slf4j.Logger;

import java.util.Arrays;

import de.iweinzierl.worktrack.model.BackupFrequency;
import de.iweinzierl.worktrack.model.BackupMetaData;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository_;
import de.iweinzierl.worktrack.persistence.LocalWorkplaceRepository_;
import de.iweinzierl.worktrack.persistence.WorkplaceRepository;
import de.iweinzierl.worktrack.util.GoogleDriveBackupHelper;

public class BackupJob extends JobService {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger(BackupJob.class.getName());

    public static final int JOB_ID = 901;

    private static final String[] SCOPES = {DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_METADATA};

    public static final String PREFS_LAST_BACKUP = "backup.job.last.backup";
    public static final String PREFS_BACKUP_FREQUENCY = "settings_backup_frequency";

    public static final long INTERVAL_DAILY = 1000L * 60 * 60 * 24;

    @Override
    public boolean onStartJob(JobParameters params) {
        LOGGER.info("Backup job started");
        backup(params);
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
        WorkplaceRepository workplaceRepository =
                LocalWorkplaceRepository_.getInstance_(getApplicationContext());

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        new GoogleDriveBackupHelper(getApplicationContext(), credential).createBackup(
                "automatic-backup",
                repository.findAll(),
                workplaceRepository.findAll(),
                new GoogleDriveBackupHelper.Callback<BackupMetaData>() {
                    @Override
                    public void onSuccess(BackupMetaData result) {
                        updateLastBackupTime();
                    }
                }
        );

        LOGGER.info("Successfully finished backupJob.");
    }

    private void updateLastBackupTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putLong(PREFS_LAST_BACKUP, DateTime.now().getMillis())
                .apply();
    }
}
