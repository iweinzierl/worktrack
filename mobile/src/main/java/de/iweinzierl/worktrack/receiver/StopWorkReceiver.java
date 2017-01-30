package de.iweinzierl.worktrack.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import de.iweinzierl.worktrack.persistence.CreationType;
import de.iweinzierl.worktrack.persistence.LocalTrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemRepository;
import de.iweinzierl.worktrack.persistence.TrackingItemType;

@EReceiver
public class StopWorkReceiver extends BroadcastReceiver {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("StopWorkReceiver");

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    @Override
    public void onReceive(Context context, Intent intent) {
        LOGGER.info("Received STOP_WORK broadcast");

        TrackingItem item = new TrackingItem(TrackingItemType.CHECKOUT, DateTime.now(), CreationType.AUTO);
        TrackingItem save = trackingItemRepository.save(item);

        if (save.getId() > 0) {
            LOGGER.info("Successfully added tracking item after checkout broadcast event");
        }
    }
}
