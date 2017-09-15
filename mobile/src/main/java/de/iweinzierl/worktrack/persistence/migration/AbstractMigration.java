package de.iweinzierl.worktrack.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractMigration implements Migration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMigration.class);

    @Override
    public boolean upgrade(SQLiteDatabase db, int version) {
        if (version != getUpgradeVersion()) {
            throw new IllegalArgumentException(
                    "Upgrade to version " + version +
                            " is not supported (only " + getUpgradeVersion() + " supported");
        }

        return true;
    }

    @Override
    public boolean downgrade(SQLiteDatabase db, int version) {
        if (version != getDowngradeVersion()) {
            throw new IllegalArgumentException(
                    "Downgrade to version " + version +
                            " is not supported (only " + getDowngradeVersion() + " supported");
        }

        return true;
    }

    void addColumn(SQLiteDatabase db, String table, String column, String type) {
        db.execSQL("ALTER TABLE '" + table + "' ADD COLUMN '" + column + "' TEXT;");
        LOGGER.debug("database migration -> added column '{}' ({}) to table '{}'", column, type, table);
    }
}
