package de.iweinzierl.worktrack.job;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import de.iweinzierl.worktrack.model.BackupFrequency;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AndroidLoggerFactory.class})
public class BackupDecisionMakerTest {

    @BeforeClass
    public static void setupMocks() {
        mockStatic(AndroidLoggerFactory.class);

        AndroidLoggerFactory loggerFactoryMock = mock(AndroidLoggerFactory.class);
        Logger loggerMock = mock(Logger.class);

        when(loggerFactoryMock.getLogger(any(String.class))).thenReturn(loggerMock);
        when(AndroidLoggerFactory.getInstance()).thenReturn(loggerFactoryMock);
    }

    @Test
    public void dailyBackupIsRequired() {
        final DateTime lastBackup = DateTime.now().minusDays(2);
        final BackupDecisionMaker decisionMaker = new BackupDecisionMaker(BackupFrequency.DAILY, lastBackup);

        assertTrue(decisionMaker.backupRequired());
    }

    @Test
    public void dailyBackupNotRequired() {
        final DateTime lastBackup = DateTime.now().withHourOfDay(1);
        final BackupDecisionMaker decisionMaker = new BackupDecisionMaker(BackupFrequency.DAILY, lastBackup);

        assertFalse(decisionMaker.backupRequired());
    }

    @Test
    public void weeklyBackupIsRequired() {
        final DateTime lastBackup = DateTime.now().withDayOfWeek(1).minusDays(1);
        final BackupDecisionMaker decisionMaker = new BackupDecisionMaker(BackupFrequency.WEEKLY, lastBackup);

        assertTrue(decisionMaker.backupRequired());
    }

    @Test
    public void weeklyBackupNotRequired() {
        final DateTime lastBackup = DateTime.now().withDayOfWeek(1);
        final BackupDecisionMaker decisionMaker = new BackupDecisionMaker(BackupFrequency.WEEKLY, lastBackup);

        assertFalse(decisionMaker.backupRequired());
    }

    @Test
    public void monthlyBackupIsRequired() {
        final DateTime lastBackup = DateTime.now().withMonthOfYear(DateTime.now().getMonthOfYear() - 1);
        final BackupDecisionMaker decisionMaker = new BackupDecisionMaker(BackupFrequency.MONTHLY, lastBackup);

        assertTrue(decisionMaker.backupRequired());
    }

    @Test
    public void monthlyBackupNotRequired() {
        final DateTime lastBackup = DateTime.now().withDayOfMonth(1);
        final BackupDecisionMaker decisionMaker = new BackupDecisionMaker(BackupFrequency.MONTHLY, lastBackup);

        assertFalse(decisionMaker.backupRequired());
    }

    @Test
    public void backupRequiredLastBackupNull() {
        assertTrue(new BackupDecisionMaker(BackupFrequency.DAILY, null).backupRequired());
        assertTrue(new BackupDecisionMaker(BackupFrequency.WEEKLY, null).backupRequired());
        assertTrue(new BackupDecisionMaker(BackupFrequency.MONTHLY, null).backupRequired());
    }

    @Test
    public void backupNotRequiredFrequencyNull() {
        assertFalse(new BackupDecisionMaker(null, DateTime.now().minusYears(1)).backupRequired());
    }

    @Test
    public void backupNotRequiredAllNull() {
        assertFalse(new BackupDecisionMaker(null, null).backupRequired());
    }
}