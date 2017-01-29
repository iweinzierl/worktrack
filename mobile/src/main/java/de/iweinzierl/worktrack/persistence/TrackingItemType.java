package de.iweinzierl.worktrack.persistence;

public enum TrackingItemType {

    CHECKIN(1),
    CHECKOUT(2);

    public final int id;

    TrackingItemType(int id) {
        this.id = id;
    }

    public static TrackingItemType byId(int id) {
        for (TrackingItemType type : values()) {
            if (type.id == id) {
                return type;
            }
        }

        return null;
    }
}
