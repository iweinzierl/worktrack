package de.iweinzierl.worktrack.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

public class MigrationV2 extends AbstractMigration {

    private static final int VERSION = 2;

    @Override
    public int getUpgradeVersion() {
        return VERSION;
    }

    @Override
    public int getDowngradeVersion() {
        return VERSION - 1;
    }

    @Override
    public boolean upgrade(SQLiteDatabase db, int version) {
        super.upgrade(db, version);
        return true;
    }

    @Override
    public boolean downgrade(SQLiteDatabase db, int version) {
        super.downgrade(db, version);
        return true;
    }
}
