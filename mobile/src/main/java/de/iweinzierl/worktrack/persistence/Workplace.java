package de.iweinzierl.worktrack.persistence;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

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

    public Workplace(double lat, double lon, double radius, String geofenceRequestId) {
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.geofenceRequestId = geofenceRequestId;
    }

    @Generated(hash = 492726427)
    public Workplace(Long id, @NotNull String name, double lat, double lon,
                     double radius, String geofenceRequestId) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.geofenceRequestId = geofenceRequestId;
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

    @Override
    public String toString() {
        return "Workplace{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", radius=" + radius +
                ", geofencingRequestId=" + geofenceRequestId +
                '}';
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
}