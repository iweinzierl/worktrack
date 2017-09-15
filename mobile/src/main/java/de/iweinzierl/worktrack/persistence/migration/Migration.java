package de.iweinzierl.worktrack.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

public interface Migration {

    int getUpgradeVersion();

    int getDowngradeVersion();

    boolean upgrade(SQLiteDatabase db, int version);

    boolean downgrade(SQLiteDatabase db, int version);
}
