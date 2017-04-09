package de.iweinzierl.worktrack.persistence;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.joda.time.DateTime;

import de.iweinzierl.worktrack.persistence.converter.CreationTypeConverter;
import de.iweinzierl.worktrack.persistence.converter.DateTimeConverter;
import de.iweinzierl.worktrack.persistence.converter.TrackingItemTypeConverter;

@Entity
public class TrackingItem {

    @Id
    private Long id;

    private Long backendId;

    @Convert(converter = TrackingItemTypeConverter.class, columnType = Integer.class)
    @NotNull
    private TrackingItemType type;

    @Convert(converter = CreationTypeConverter.class, columnType = Integer.class)
    @NotNull
    private CreationType creationType;

    @Convert(converter = DateTimeConverter.class, columnType = String.class)
    @NotNull
    private DateTime eventTime;

    public TrackingItem(TrackingItemType type, DateTime eventTime, CreationType creationType) {
        this.type = type;
        this.creationType = creationType;
        this.eventTime = eventTime;
    }

    protected TrackingItem(Long id, TrackingItemType type, DateTime eventTime) {
        this.id = id;
        this.type = type;
        this.eventTime = eventTime;
    }

    @Generated(hash = 259717695)
    public TrackingItem(Long id, Long backendId, @NotNull TrackingItemType type,
                        @NotNull CreationType creationType, @NotNull DateTime eventTime) {
        this.id = id;
        this.backendId = backendId;
        this.type = type;
        this.creationType = creationType;
        this.eventTime = eventTime;
    }

    @Generated(hash = 969341189)
    public TrackingItem() {
    }

    public Long getId() {
        return id;
    }

    public TrackingItemType getType() {
        return type;
    }

    public CreationType getCreationType() {
        return creationType;
    }

    public DateTime getEventTime() {
        return eventTime;
    }

    public Long getBackendId() {
        return backendId;
    }

    public void setBackendId(Long backendId) {
        this.backendId = backendId;
    }

    @Override
    public String toString() {
        return "TrackingItem{" +
                "id=" + id +
                ", backendId=" + backendId +
                ", type=" + type +
                ", creationType=" + creationType +
                ", eventTime=" + eventTime +
                '}';
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setType(TrackingItemType type) {
        this.type = type;
    }

    public void setEventTime(DateTime eventTime) {
        this.eventTime = eventTime;
    }

    public void setCreationType(CreationType creationType) {
        this.creationType = creationType;
    }

}
