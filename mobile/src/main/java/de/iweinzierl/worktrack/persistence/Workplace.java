package de.iweinzierl.worktrack.persistence;

import com.google.common.base.Objects;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.joda.time.DateTime;

import de.iweinzierl.worktrack.persistence.converter.DateTimeConverter;

@Entity
public class Workplace {

    @Id
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private double lat;

    @NotNull
    private double lon;

    @NotNull
    private double radius;

    private String geofenceRequestId;

    @Convert(converter = DateTimeConverter.class, columnType = String.class)
    private DateTime registeredAt;

    public Workplace(double lat, double lon, double radius, String geofenceRequestId) {
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.geofenceRequestId = geofenceRequestId;
    }

    @Generated(hash = 322495058)
    public Workplace(Long id, @NotNull String name, double lat, double lon, double radius,
                     String geofenceRequestId, DateTime registeredAt) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.geofenceRequestId = geofenceRequestId;
        this.registeredAt = registeredAt;
    }

    @Generated(hash = 1995917923)
    public Workplace() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public double getLat() {
        return this.lat;
    }

    public double getLon() {
        return this.lon;
    }

    public double getRadius() {
        return this.radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getGeofenceRequestId() {
        return this.geofenceRequestId;
    }

    public void setGeofenceRequestId(String geofenceRequestId) {
        this.geofenceRequestId = geofenceRequestId;
    }

    public DateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(DateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Workplace workplace = (Workplace) o;
        return Double.compare(workplace.lat, lat) == 0 &&
                Double.compare(workplace.lon, lon) == 0 &&
                Double.compare(workplace.radius, radius) == 0 &&
                Objects.equal(id, workplace.id) &&
                Objects.equal(name, workplace.name) &&
                Objects.equal(geofenceRequestId, workplace.geofenceRequestId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, lat, lon, radius, geofenceRequestId);
    }

    @Override
    public String toString() {
        return "Workplace{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", radius=" + radius +
                ", geofencingRequestId=" + geofenceRequestId +
                ", registeredAt=" + registeredAt +
                '}';
    }
}
