package de.iweinzierl.worktrack.job;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.joda.time.DateTime;
import org.slf4j.Logger;

import de.iweinzierl.worktrack.model.BackupFrequency;

class BackupDecisionMaker {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger(BackupDecisionMaker.class.getName());

    private final BackupFrequency backupFrequency;
    private final DateTime lastBackupTime;

    BackupDecisionMaker(BackupFrequency backupFrequency, DateTime lastBackupTime) {
        this.backupFrequency = backupFrequency;
        this.lastBackupTime = lastBackupTime;
    }

    boolean backupRequired() {
        if (lastBackupTime == null && backupFrequency != null && backupFrequency != BackupFrequency.NEVER) {
            LOGGER.debug("BackupJob never run before.");
            return true;
        } else if (backupFrequency == null) {
            LOGGER.warn("No backup frequency specified!");
            return false;
        }

        LOGGER.debug("Backup frequency set -> {}", backupFrequency);
        LOGGER.debug("Last backup time     -> {}", lastBackupTime);

        final DateTime now = DateTime.now();

        switch (backupFrequency) {
            case DAILY:
                return lastBackupTime.getYear() <= now.getYear()
                        && lastBackupTime.getDayOfYear() < now.getDayOfYear();
            case WEEKLY:
                return lastBackupTime.getYear() <= now.getYear()
                        && lastBackupTime.getWeekOfWeekyear() < now.getWeekOfWeekyear();
            case MONTHLY:
                return lastBackupTime.getYear() <= now.getYear()
                        && lastBackupTime.getMonthOfYear() < now.getMonthOfYear();
            default:
                return false;
        }
    }
}
