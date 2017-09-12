package de.iweinzierl.worktrack.model;

import java.util.List;

import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.Workplace;

public class Backup {

    private BackupMetaData metaData;
    private List<TrackingItem> trackingItems;
    private List<Workplace> workplaces;

    public Backup(BackupMetaData metaData, List<TrackingItem> trackingItems, List<Workplace> workplaces) {
        this.metaData = metaData;
        this.trackingItems = trackingItems;
        this.workplaces = workplaces;
    }

    public BackupMetaData getMetaData() {
        return metaData;
    }

    public List<TrackingItem> getTrackingItems() {
        return trackingItems;
    }

    public List<Workplace> getWorkplaces() {
        return workplaces;
    }
}
