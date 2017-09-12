package de.iweinzierl.worktrack;

import android.app.Application;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.util.ProductFlavor;

@EApplication
public class WorktrackApplication extends Application {

    public static final String PACKAGE_NAME_PRO = "de.iweinzierl.worktrack";
    public static final String PACKAGE_NAME_FREE = "de.iweinzierl.worktrack.free";

    public static final String LOG_TAG = "[WT]";

    private static WorktrackApplication INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(WorktrackApplication.class);

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    public static WorktrackApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (INSTANCE == null) {
            INSTANCE = this;
        }
    }

    @AfterInject
    public void logStatistics() {
        List<TrackingItem> trackingItems = trackingItemRepository.findAll();
        LOGGER.info("Found {} tracking items in database", trackingItems.size());
    }

    public ProductFlavor getProductFlavor() {
        if (getPackageName().equals(PACKAGE_NAME_PRO)) {
            return ProductFlavor.PRO;
        } else if (getPackageName().equals(PACKAGE_NAME_FREE)) {
            return ProductFlavor.FREE;
        } else {
            return ProductFlavor.DEV;
        }
    }
}
