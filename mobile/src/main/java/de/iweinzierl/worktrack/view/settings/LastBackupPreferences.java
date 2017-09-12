package de.iweinzierl.worktrack.view.settings;

public enum LastBackupPreferences {

    SIZE("de.iweinzierl.worktrack.lastbackup.size"),
    DATE("de.iweinzierl.worktrack.lastbackup.date"),
    ITEMS("de.iweinzierl.worktrack.lastbackup.items"),
    WORKPLACES("de.iweinzierl.worktrack.lastbackup.workplaces");

    private String property;

    LastBackupPreferences(String property) {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}
