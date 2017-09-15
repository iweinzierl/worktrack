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

    private String workplaceName;
    private long workplaceId;
    private double workplaceLat;
    private double workplaceLon;
    private double triggerEventLat;
    private double triggerEventLon;

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

    public TrackingItem(Long id, Long backendId, @NotNull TrackingItemType type,
                        @NotNull CreationType creationType, @NotNull DateTime eventTime) {
        this.id = id;
        this.backendId = backendId;
        this.type = type;
        this.creationType = creationType;
        this.eventTime = eventTime;
    }

    @Generated(hash = 1420529215)
    public TrackingItem(Long id, Long backendId, @NotNull TrackingItemType type,
                        @NotNull CreationType creationType, @NotNull DateTime eventTime, String workplaceName,
                        long workplaceId, double workplaceLat, double workplaceLon, double triggerEventLat,
                        double triggerEventLon) {
        this.id = id;
        this.backendId = backendId;
        this.type = type;
        this.creationType = creationType;
        this.eventTime = eventTime;
        this.workplaceName = workplaceName;
        this.workplaceId = workplaceId;
        this.workplaceLat = workplaceLat;
        this.workplaceLon = workplaceLon;
        this.triggerEventLat = triggerEventLat;
        this.triggerEventLon = triggerEventLon;
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

    public String getWorkplaceName() {
        return workplaceName;
    }

    public long getWorkplaceId() {
        return workplaceId;
    }

    public double getWorkplaceLat() {
        return workplaceLat;
    }

    public double getWorkplaceLon() {
        return workplaceLon;
    }

    public double getTriggerEventLat() {
        return triggerEventLat;
    }

    public double getTriggerEventLon() {
        return triggerEventLon;
    }

    public void setBackendId(Long backendId) {
        this.backendId = backendId;
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

    public void setWorkplaceName(String workplaceName) {
        this.workplaceName = workplaceName;
    }

    public void setWorkplaceId(long workplaceId) {
        this.workplaceId = workplaceId;
    }

    public void setWorkplaceLat(double workplaceLat) {
        this.workplaceLat = workplaceLat;
    }

    public void setWorkplaceLon(double workplaceLon) {
        this.workplaceLon = workplaceLon;
    }

    public void setTriggerEventLat(double triggerEventLat) {
        this.triggerEventLat = triggerEventLat;
    }

    public void setTriggerEventLon(double triggerEventLon) {
        this.triggerEventLon = triggerEventLon;
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

}
