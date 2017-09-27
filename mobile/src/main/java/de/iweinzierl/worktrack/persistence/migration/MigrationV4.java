package de.iweinzierl.worktrack.persistence.migration;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.common.collect.Lists;

import java.util.List;

import de.iweinzierl.worktrack.persistence.TrackingItemDao;
import de.iweinzierl.worktrack.persistence.WorkplaceDao;

public class MigrationV4 extends AbstractMigration {

    private static final int VERSION = 4;

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
        addColumn(db, WorkplaceDao.TABLENAME, WorkplaceDao.Properties.RegisteredAt.columnName, "TEXT");
        db.setTransactionSuccessful();
        db.endTransaction();

        return true;
    }

    @Override
    public boolean downgrade(SQLiteDatabase db, int version) {
        super.downgrade(db, version);

        db.beginTransaction();
        List<String> updatedTableColumns = Lists.newArrayList(
                WorkplaceDao.Properties.Id.columnName,
                WorkplaceDao.Properties.Name.columnName,
                WorkplaceDao.Properties.Lat.columnName,
                WorkplaceDao.Properties.Lon.columnName,
                WorkplaceDao.Properties.GeofenceRequestId.columnName,
                WorkplaceDao.Properties.Radius.columnName
        );

        String columnsSeperated = TextUtils.join(",", updatedTableColumns);

        db.execSQL("ALTER TABLE " + WorkplaceDao.TABLENAME
                + " RENAME TO " + WorkplaceDao.TABLENAME + "_old;");

        db.execSQL("CREATE TABLE " + WorkplaceDao.TABLENAME + " ("
                + WorkplaceDao.Properties.Id.columnName + " INTEGER PRIMARY KEY,"
                + WorkplaceDao.Properties.Name.columnName + " TEXT,"
                + WorkplaceDao.Properties.Lat.columnName + " REAL,"
                + WorkplaceDao.Properties.Lon.columnName + " REAL,"
                + WorkplaceDao.Properties.GeofenceRequestId + " TEXT,"
                + WorkplaceDao.Properties.Radius.columnName + " REAL);"
        );

        db.execSQL("INSERT INTO " + WorkplaceDao.TABLENAME + "(" + columnsSeperated + ")"
                + "SELECT " + columnsSeperated + " FROM " + WorkplaceDao.TABLENAME + "_old;");

        db.execSQL("DROP TABLE " + TrackingItemDao.TABLENAME + "_old;");
        db.setTransactionSuccessful();
        db.endTransaction();

        return true;
    }
}
