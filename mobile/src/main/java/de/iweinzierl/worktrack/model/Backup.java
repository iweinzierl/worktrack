package de.iweinzierl.worktrack.model;

import org.joda.time.LocalDateTime;

public class Backup {

    private String driveId;
    private String title;
    private long size;
    private LocalDateTime lastModified;

    public Backup(String driveId, String title, long size, LocalDateTime lastModified) {
        this.driveId = driveId;
        this.title = title;
        this.size = size;
        this.lastModified = lastModified;
    }

    public String getDriveId() {
        return driveId;
    }

    public String getTitle() {
        return title;
    }

    public long getSize() {
        return size;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return "Backup{" +
                "driveId='" + driveId + '\'' +
                ", title='" + title + '\'' +
                ", size=" + size +
                ", lastModified=" + lastModified +
                '}';
    }
}
