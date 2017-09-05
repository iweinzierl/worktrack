package de.iweinzierl.worktrack.receiver;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.android.gms.location.GeofencingEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeofencingTransitionService extends IntentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeofencingTransitionService.class);

    public GeofencingTransitionService() {
        super("GeofencingTransitionService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event == null) {
            LOGGER.warn("Received geofencing event but event is null");
            return;
        }

        LOGGER.info("Received geofencing transition intent: {}", event.getGeofenceTransition());
    }
}
