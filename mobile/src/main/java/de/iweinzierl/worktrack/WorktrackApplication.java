package de.iweinzierl.worktrack;

import android.app.Application;
import android.content.pm.PackageManager;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;
import org.slf4j.Logger;

import java.util.List;

import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;

@EApplication
public class WorktrackApplication extends Application {

    public static final String PACKAGE_NAME_PRO = "de.iweinzierl.worktrack";
    public static final String LOG_TAG = "[WT]";

    private static WorktrackApplication INSTANCE;

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance(LOG_TAG).getLogger(WorktrackApplication.class.getName());

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

    public boolean isPro() {
        return getPackageName().equals(PACKAGE_NAME_PRO);
    }
}
