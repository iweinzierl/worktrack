package de.iweinzierl.worktrack.persistence;

import android.content.Context;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.slf4j.Logger;

import de.iweinzierl.worktrack.persistence.migration.UpgradeHelper;

@EBean(scope = EBean.Scope.Singleton)
public class DaoSessionFactory {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("DaoSessionFactory");
    private static final String DB_NAME = "worktrack-db";

    @RootContext
    protected Context context;

    private DaoMaster daoMaster;

    @AfterInject
    public void init() {
        LOGGER.info("Initialize session factory");

        UpgradeHelper helper = new UpgradeHelper(context, DB_NAME);
        daoMaster = new DaoMaster(helper.getWritableDb());
    }

    public DaoSession getSession() {
        LOGGER.info("create new dao session");

        return daoMaster.newSession();
    }
}
