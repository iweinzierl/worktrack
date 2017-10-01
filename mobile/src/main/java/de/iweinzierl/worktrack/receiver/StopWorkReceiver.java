package de.iweinzierl.worktrack.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import de.iweinzierl.worktrack.analytics.AnalyticsEvents;
import de.iweinzierl.worktrack.analytics.AnalyticsParams;
import de.iweinzierl.worktrack.persistence.CreationType;
import de.iweinzierl.worktrack.persistence.repository.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.repository.TrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItemType;
import de.iweinzierl.worktrack.persistence.repository.exception.PersistenceException;

@EReceiver
public class StopWorkReceiver extends BroadcastReceiver {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("StopWorkReceiver");

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    @Override
    public void onReceive(Context context, Intent intent) {
        LOGGER.info("Received STOP_WORK broadcast");

        TrackingItem item = new TrackingItem(TrackingItemType.CHECKOUT, DateTime.now(), CreationType.AUTO);

        try {
            TrackingItem save = trackingItemRepository.save(item);
            if (save.getId() > 0) {
                LOGGER.info("Successfully added tracking item after checkout broadcast event");
            }
        }
        catch (PersistenceException e) {
            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsParams.ERROR_MESSAGE.name(), e.getMessage());
            FirebaseAnalytics.getInstance(context)
                    .logEvent(AnalyticsEvents.TRACKING_ITEM_SAVE_FAILURE.name(), bundle);
        }
    }
}
