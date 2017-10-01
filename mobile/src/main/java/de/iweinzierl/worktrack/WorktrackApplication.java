package de.iweinzierl.worktrack;

import android.app.Application;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;
import org.slf4j.Logger;

import de.iweinzierl.worktrack.persistence.repository.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.repository.TrackingItemRepository;
import de.iweinzierl.worktrack.util.ProductFlavor;

@EApplication
public class WorktrackApplication extends Application {

    public static final String PACKAGE_NAME_PRO = "de.iweinzierl.worktrack";
    public static final String PACKAGE_NAME_FREE = "de.iweinzierl.worktrack.free";

    public static final String LOG_TAG = "[WT]";

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance(LOG_TAG).getLogger("WorktrackApplication");

    private static WorktrackApplication INSTANCE;

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

    public ProductFlavor getProductFlavor() {
        if (getPackageName().equals(PACKAGE_NAME_PRO)) {
            return ProductFlavor.PRO;
        } else if (getPackageName().equals(PACKAGE_NAME_FREE)) {
            return ProductFlavor.FREE;
        } else {
            return ProductFlavor.DEV;
        }
    }

    public boolean isPro() {
        return getProductFlavor() == ProductFlavor.PRO
                || getProductFlavor() == ProductFlavor.DEV;
    }
}
