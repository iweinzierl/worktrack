package de.iweinzierl.worktrack.persistence.migration;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.common.collect.Lists;

import java.util.List;

import de.iweinzierl.worktrack.persistence.TrackingItemDao;

public class MigrationV3 extends AbstractMigration {

    private static final int VERSION = 3;

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

        db.beginTransaction();
        addColumn(db, TrackingItemDao.TABLENAME, TrackingItemDao.Properties.WorkplaceId.columnName, "INTEGER");
        addColumn(db, TrackingItemDao.TABLENAME, TrackingItemDao.Properties.WorkplaceName.columnName, "TEXT");
        addColumn(db, TrackingItemDao.TABLENAME, TrackingItemDao.Properties.WorkplaceLat.columnName, "REAL");
        addColumn(db, TrackingItemDao.TABLENAME, TrackingItemDao.Properties.WorkplaceLon.columnName, "REAL");
        addColumn(db, TrackingItemDao.TABLENAME, TrackingItemDao.Properties.TriggerEventLat.columnName, "REAL");
        addColumn(db, TrackingItemDao.TABLENAME, TrackingItemDao.Properties.TriggerEventLon.columnName, "REAL");
        db.setTransactionSuccessful();
        db.endTransaction();

        return true;
    }

    @Override
    public boolean downgrade(SQLiteDatabase db, int version) {
        super.downgrade(db, version);

        db.beginTransaction();
        List<String> updatedTableColumns = Lists.newArrayList(
                TrackingItemDao.Properties.Id.columnName,
                TrackingItemDao.Properties.BackendId.columnName,
                TrackingItemDao.Properties.Type.columnName,
                TrackingItemDao.Properties.CreationType.columnName,
                TrackingItemDao.Properties.EventTime.columnName
        );

        String columnsSeperated = TextUtils.join(",", updatedTableColumns);

        db.execSQL("ALTER TABLE " + TrackingItemDao.TABLENAME
                + " RENAME TO " + TrackingItemDao.TABLENAME + "_old;");

        db.execSQL("CREATE TABLE " + TrackingItemDao.TABLENAME + " ("
                + TrackingItemDao.Properties.Id.columnName + " INTEGER PRIMARY KEY,"
                + TrackingItemDao.Properties.BackendId.columnName + " INTEGER,"
                + TrackingItemDao.Properties.Type.columnName + " INTEGER,"
                + TrackingItemDao.Properties.CreationType.columnName + " INTEGER,"
                + TrackingItemDao.Properties.EventTime.columnName + " TEXT);"
        );

        db.execSQL("INSERT INTO " + TrackingItemDao.TABLENAME + "(" + columnsSeperated + ")"
                + "SELECT " + columnsSeperated + " FROM " + TrackingItemDao.TABLENAME + "_old;");

        db.execSQL("DROP TABLE " + TrackingItemDao.TABLENAME + "_old;");
        db.setTransactionSuccessful();
        db.endTransaction();

        return true;
    }
}
