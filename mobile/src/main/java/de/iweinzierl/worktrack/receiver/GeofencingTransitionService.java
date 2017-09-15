package de.iweinzierl.worktrack.receiver;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iweinzierl.worktrack.persistence.LocalWorkplaceRepository;
import de.iweinzierl.worktrack.persistence.LocalWorkplaceRepository_;
import de.iweinzierl.worktrack.persistence.Workplace;

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

        LOGGER.info("Received geofencing transition intent: {}", event.toString());

        if (event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {
            processEnterTransitionEvent(event);
        } else if (event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_EXIT) {
            processExitTransitionEvent(event);
        } else {
            LOGGER.warn("Received unknown transition: {}", event.getGeofenceTransition());
        }
    }

    private void processEnterTransitionEvent(GeofencingEvent event) {
        Workplace matchingWorkplace = findMatchingWorkplace(event);
        if (matchingWorkplace == null) {
            LOGGER.info("Ignore geofencing event -> does not match any workplace");
            return;
        }

        Intent intent = new Intent("de.iweinzierl.worktrack.START_WORK");
        intent.putExtra(StartWorkReceiver.EXTRA_WORKPLACE_ID, matchingWorkplace.getId());
        intent.putExtra(StartWorkReceiver.EXTRA_WORKPLACE_NAME, matchingWorkplace.getName());
        intent.putExtra(StartWorkReceiver.EXTRA_WORKPLACE_LAT, matchingWorkplace.getLat());
        intent.putExtra(StartWorkReceiver.EXTRA_WORKPLACE_LON, matchingWorkplace.getLon());
        intent.putExtra(StartWorkReceiver.EXTRA_EVENT_LAT, event.getTriggeringLocation().getLatitude());
        intent.putExtra(StartWorkReceiver.EXTRA_EVENT_LON, event.getTriggeringLocation().getLongitude());
        sendBroadcast(intent);
    }

    private void processExitTransitionEvent(GeofencingEvent event) {
        sendBroadcast(new Intent("de.iweinzierl.worktrack.STOP_WORK"));
    }

    private Workplace findMatchingWorkplace(GeofencingEvent event) {
        LocalWorkplaceRepository repository = LocalWorkplaceRepository_.getInstance_(getApplicationContext());

        Location triggeringLocation = event.getTriggeringLocation();

        for (Workplace workplace : repository.findAll()) {
            Location tmp = new Location("tmp");
            tmp.setLatitude(workplace.getLat());
            tmp.setLongitude(workplace.getLon());

            float distance = triggeringLocation.distanceTo(tmp);

            if (distance <= workplace.getRadius()) {
                return workplace;
            }
        }

        return null;
    }
}
