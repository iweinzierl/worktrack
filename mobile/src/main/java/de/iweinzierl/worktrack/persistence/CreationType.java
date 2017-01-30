package de.iweinzierl.worktrack.persistence;

public enum CreationType {

    AUTO(1),
    MANUAL(2);

    public final int id;

    CreationType(int id) {
        this.id = id;
    }

    public static CreationType byId(int id) {
        for (CreationType type : values()) {
            if (type.id == id) {
                return type;
            }
        }

        return null;
    }
}
