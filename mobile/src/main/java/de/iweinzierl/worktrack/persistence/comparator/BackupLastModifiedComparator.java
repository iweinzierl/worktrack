package de.iweinzierl.worktrack.persistence.comparator;

import de.iweinzierl.worktrack.model.BackupMetaData;

public class BackupLastModifiedComparator implements java.util.Comparator<BackupMetaData> {
    @Override
    public int compare(BackupMetaData a, BackupMetaData b) {
        return b.getLastModified().compareTo(a.getLastModified());
    }
}
