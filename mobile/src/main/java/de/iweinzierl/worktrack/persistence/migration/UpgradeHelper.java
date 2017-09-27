package de.iweinzierl.worktrack.persistence.migration;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iweinzierl.worktrack.persistence.DaoMaster;

public class UpgradeHelper extends DaoMaster.OpenHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeHelper.class);
    private static final SparseArray<Object> migrations = new SparseArray<>();

    static {
        migrations.append(2, new MigrationV2());
        migrations.append(3, new MigrationV3());
        migrations.append(4, new MigrationV4());
    }

    public UpgradeHelper(Context context, String name) {
        super(context, name);
    }

    public UpgradeHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOGGER.info("Update database: version {} -> {}", oldVersion, newVersion);

        for (int upgradeVersion = oldVersion + 1; upgradeVersion <= newVersion; upgradeVersion++) {
            upgradeVersion(db, upgradeVersion);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOGGER.info("Downgrade database: version {} -> {}", oldVersion, newVersion);

        for (int downgradeVersion = oldVersion - 1; downgradeVersion >= 1; downgradeVersion--) {
            downgradeVersion(db, downgradeVersion);
        }
    }

    private void upgradeVersion(SQLiteDatabase db, int upgradeVersion) {
        LOGGER.info("Upgrade database to version: {}", upgradeVersion);

        Migration migration = (Migration) migrations.get(upgradeVersion);

        if (migration == null) {
            throw new IllegalStateException("No migration script for version " + upgradeVersion + " found!");
        }

        migration.upgrade(db, upgradeVersion);
    }

    private void downgradeVersion(SQLiteDatabase db, int downgradeVersion) {
        LOGGER.info("Downgrade database to version: {}", downgradeVersion);

        Migration migration = (Migration) migrations.get(downgradeVersion + 1);

        if (migration == null) {
            throw new IllegalStateException("No migration script for version " + downgradeVersion + " found!");
        }

        migration.downgrade(db, downgradeVersion);
    }
}
