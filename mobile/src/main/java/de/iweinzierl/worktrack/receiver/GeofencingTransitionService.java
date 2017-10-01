package de.iweinzierl.worktrack.receiver;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.iweinzierl.worktrack.analytics.AnalyticsEvents;
import de.iweinzierl.worktrack.analytics.AnalyticsParams;
import de.iweinzierl.worktrack.persistence.repository.LocalWorkplaceRepository;
import de.iweinzierl.worktrack.persistence.Workplace;
import de.iweinzierl.worktrack.persistence.repository.WorkplaceRepository;

@EService
public class GeofencingTransitionService extends IntentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeofencingTransitionService.class);

    @Bean(value = LocalWorkplaceRepository.class)
    WorkplaceRepository workplaceRepository;

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
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(getApplicationContext());

        Workplace matchingWorkplace = findMatchingWorkplace(event);
        if (matchingWorkplace == null) {
            LOGGER.info("Ignore geofencing event -> does not match any workplace");
            analytics.logEvent(AnalyticsEvents.GEOFENCE_ENTER_BUT_NO_WORKPLACE.name(), null);
            return;
        }

        analytics.logEvent(AnalyticsEvents.GEOFENCE_ENTER.name(), null);

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
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(getApplicationContext());
        Bundle analyticsExtras = new Bundle();

        if (isAutomaticExitEvent(event)) {
            LOGGER.info("Ignore automatic exit event which is generated after Geofence creation");
            analytics.logEvent(AnalyticsEvents.GEOFENCE_EXIT_IGNORE_AUTOMATED_EXIT.name(), null);

            return;
        }

        if (event.hasError()) {
            analyticsExtras.putString(AnalyticsParams.ERROR_CODE.name(), String.valueOf(event.getErrorCode()));
        }

        analytics.logEvent(AnalyticsEvents.GEOFENCE_EXIT.name(), analyticsExtras);
        sendBroadcast(new Intent("de.iweinzierl.worktrack.STOP_WORK"));
    }

    private Workplace findMatchingWorkplace(GeofencingEvent event) {
        Location triggeringLocation = event.getTriggeringLocation();

        for (Workplace workplace : workplaceRepository.findAll()) {
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

    /**
     * After registering a new geofence, Google sends automatically an exit event when the device
     * is outside the geofence while creation. This method defines a 1 minute buffer when exit
     * events are ignored (compares now - 1minute to the registration time of the geofence if the
     * registration date is not null).
     *
     * @param event The geofence event.
     * @return true if the geofence was only registered within the last minute and the event is an
     * exit event.
     */
    private boolean isAutomaticExitEvent(GeofencingEvent event) {
        Workplace lastWorkplace = getLastAddedWorkplace();

        return event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_EXIT
                && lastWorkplace != null
                && lastWorkplace.getRegisteredAt() != null
                && DateTime.now().minusMinutes(1).isBefore(lastWorkplace.getRegisteredAt());
    }

    private Workplace getLastAddedWorkplace() {
        List<Workplace> workplaces = workplaceRepository.findAll();
        Collections.sort(workplaces, new Comparator<Workplace>() {
            @Override
            public int compare(Workplace workplace, Workplace t1) {
                return workplace.getId().compareTo(t1.getId());
            }
        });

        return !workplaces.isEmpty()
                ? workplaces.get(0)
                : null;
    }
}
