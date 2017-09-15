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
public class StartWorkReceiver extends BroadcastReceiver {

    public static final String EXTRA_WORKPLACE_ID = "de.iweinzierl.worktrack.receiver.extra.workplace_id";
    public static final String EXTRA_WORKPLACE_NAME = "de.iweinzierl.worktrack.receiver.extra.workplace_name";
    public static final String EXTRA_WORKPLACE_LAT = "de.iweinzierl.worktrack.receiver.extra.workplace_lat";
    public static final String EXTRA_WORKPLACE_LON = "de.iweinzierl.worktrack.receiver.extra.workplace_lon";
    public static final String EXTRA_EVENT_LAT = "de.iweinzierl.worktrack.receiver.extra.event.lat";
    public static final String EXTRA_EVENT_LON = "de.iweinzierl.worktrack.receiver.extra.event.lon";

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("StartWorkReceiver");

    @Bean(LocalTrackingItemRepository.class)
    TrackingItemRepository trackingItemRepository;

    @Override
    public void onReceive(Context context, Intent intent) {
        LOGGER.info("Received START_WORK broadcast");

        TrackingItem item = new TrackingItem(TrackingItemType.CHECKIN, DateTime.now(), CreationType.AUTO);

        if (intent.hasExtra(EXTRA_WORKPLACE_NAME)) {
            item.setWorkplaceName(intent.getStringExtra(EXTRA_WORKPLACE_NAME));
        }

        if (intent.hasExtra(EXTRA_WORKPLACE_ID)) {
            item.setWorkplaceId(intent.getLongExtra(EXTRA_WORKPLACE_ID, 0));
        }

        if (intent.hasExtra(EXTRA_WORKPLACE_LAT)) {
            item.setWorkplaceLat(intent.getDoubleExtra(EXTRA_WORKPLACE_LAT, 0));
        }

        if (intent.hasExtra(EXTRA_WORKPLACE_LON)) {
            item.setWorkplaceLon(intent.getDoubleExtra(EXTRA_WORKPLACE_LON, 0));
        }

        if (intent.hasExtra(EXTRA_EVENT_LAT)) {
            item.setTriggerEventLat(intent.getDoubleExtra(EXTRA_EVENT_LAT, 0));
        }

        if (intent.hasExtra(EXTRA_EVENT_LON)) {
            item.setTriggerEventLon(intent.getDoubleExtra(EXTRA_EVENT_LON, 0));
        }

        TrackingItem save = trackingItemRepository.save(item);

        if (save.getId() > 0) {
            LOGGER.info("Successfully added tracking item after checkin broadcast event");
        }
    }
}
