package de.iweinzierl.worktrack.model;

import android.os.Build;

import org.joda.time.LocalDateTime;

public class BackupMetaData {

    private String driveId;
    private String title;

    private int itemCount;

    private int workplaceCount;

    private long size;
    private LocalDateTime lastModified;

    public BackupMetaData(String driveId, String title, long size, LocalDateTime lastModified) {
        this.driveId = driveId;
        this.title = title;
        this.size = size;
        this.lastModified = lastModified;
    }

    private BackupMetaData(Builder builder) {
        driveId = builder.driveId;
        title = builder.title;
        itemCount = builder.itemCount;
        workplaceCount = builder.workplaceCount;
        size = builder.size;
        lastModified = builder.lastModified;
    }

    public String getDriveId() {
        return driveId;
    }

    public String getTitle() {
        return title;
    }

    public int getItemCount() {
        return itemCount;
    }

    public int getWorkplaceCount() {
        return workplaceCount;
    }

    public long getSize() {
        return size;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return "BackupMetaData{" +
                "driveId='" + driveId + '\'' +
                ", title='" + title + '\'' +
                ", itemCount=" + itemCount +
                ", size=" + size +
                ", lastModified=" + lastModified +
                '}';
    }


    public static final class Builder {
        private String driveId;
        private String title;
        private int itemCount;
        private int workplaceCount;
        private long size;
        private LocalDateTime lastModified;

        public Builder() {
        }

        public Builder driveId(String val) {
            driveId = val;
            return this;
        }

        public Builder title(String val) {
            title = val;
            return this;
        }

        public Builder itemCount(int val) {
            itemCount = val;
            return this;
        }

        public Builder workplaceCount(int val) {
            workplaceCount = val;
            return this;
        }

        public Builder size(long val) {
            size = val;
            return this;
        }

        public Builder lastModified(LocalDateTime val) {
            lastModified = val;
            return this;
        }

        public BackupMetaData build() {
            return new BackupMetaData(this);
        }
    }
}
