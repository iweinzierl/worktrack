package de.iweinzierl.worktrack.model;

import java.util.List;

import de.iweinzierl.worktrack.persistence.TrackingItem;

public class Backup {

    private BackupMetaData metaData;
    private List<TrackingItem> trackingItems;

    public Backup(BackupMetaData metaData, List<TrackingItem> trackingItems) {
        this.metaData = metaData;
        this.trackingItems = trackingItems;
    }

    public BackupMetaData getMetaData() {
        return metaData;
    }

    public List<TrackingItem> getTrackingItems() {
        return trackingItems;
    }
}
