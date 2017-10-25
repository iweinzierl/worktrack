package de.iweinzierl.worktrack.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.api.support.app.AbstractIntentService;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import java.util.List;

import de.iweinzierl.worktrack.analytics.AnalyticsEvents;
import de.iweinzierl.worktrack.analytics.AnalyticsParams;
import de.iweinzierl.worktrack.persistence.Workplace;
import de.iweinzierl.worktrack.persistence.repository.RepositoryFactory;
import de.iweinzierl.worktrack.persistence.repository.WorkplaceRepository;
import de.iweinzierl.worktrack.receiver.GeofencingTransitionService_;
import de.iweinzierl.worktrack.util.BundleBuilder;

@EIntentService
public class UpdateGeofenceService extends AbstractIntentService {

    public static final String SERVICE_NAME = "UpdateGeofenceService";

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger(SERVICE_NAME);

    private GeofencingClient geofencingClient;
    private WorkplaceRepository workplaceRepository;

    public UpdateGeofenceService() {
        super(SERVICE_NAME);
    }

    @Bean
    void setRepositoryFactory(RepositoryFactory repositoryFactory) {
        workplaceRepository = repositoryFactory.getWorkplaceRepository();
    }

    @ServiceAction
    void deleteAction(final String geofenceId) {
        LOGGER.info("Received 'deleteAction' call");
        geofencingClient = LocationServices.getGeofencingClient(getApplicationContext());
        geofencingClient.removeGeofences(Lists.newArrayList(geofenceId))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        LOGGER.info("Removing geofence '{}' completed.", geofenceId);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        LOGGER.info("Removing geofence '{}' successful.", geofenceId);
                        logAnalyticsEvent(AnalyticsEvents.GEOFENCE_REMOVE_SUCCESS, null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        LOGGER.info("Removing geofence '{}' failed.", geofenceId);
                        logAnalyticsEvent(AnalyticsEvents.GEOFENCE_REMOVE_FAILURE,
                                new BundleBuilder()
                                        .withString(AnalyticsParams.ERROR_MESSAGE.name(), e.getMessage())
                                        .build());
                    }
                });
    }

    @ServiceAction
    void updateAction() {
        LOGGER.info("Received 'updateAction' call");
        geofencingClient = LocationServices.getGeofencingClient(getApplicationContext());

        geofencingClient.removeGeofences(buildGeofencePendingIntent())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        LOGGER.info("Removing geofences completed.");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        LOGGER.info("Removing geofences successful.");
                        logAnalyticsEvent(AnalyticsEvents.GEOFENCE_REMOVE_SUCCESS, null);
                        UpdateGeofenceService_.intent(UpdateGeofenceService.this).addAction().start();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        LOGGER.info("Removing geofences failed.");
                        logAnalyticsEvent(AnalyticsEvents.GEOFENCE_REMOVE_FAILURE,
                                new BundleBuilder()
                                        .withString(AnalyticsParams.ERROR_MESSAGE.name(), e.getMessage())
                                        .build());
                    }
                });
    }

    @ServiceAction
    void addAction() {
        LOGGER.info("Received 'addAction' call");
        geofencingClient = LocationServices.getGeofencingClient(getApplicationContext());

        GeofencingRequest geofencingRequest = buildGeofencingRequest(workplaceRepository.findAll());
        PendingIntent geofencingIntent = buildGeofencePendingIntent();

        try {
            geofencingClient.addGeofences(geofencingRequest, geofencingIntent)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            LOGGER.info("Adding geofences completed.");
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            LOGGER.info("Adding geofences successful.");
                            logAnalyticsEvent(AnalyticsEvents.GEOFENCE_ADD_SUCCESS, null);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            LOGGER.info("Adding geofences failed.");
                            logAnalyticsEvent(AnalyticsEvents.GEOFENCE_ADD_FAILURE,
                                    new BundleBuilder()
                                            .withString(AnalyticsParams.ERROR_MESSAGE.name(), e.getMessage())
                                            .build());
                        }
                    });
        } catch (SecurityException e) {
            logAnalyticsEvent(AnalyticsEvents.GEOFENCE_ADD_FAILURE,
                    new BundleBuilder()
                            .withString(AnalyticsParams.ERROR_MESSAGE.name(), e.getMessage())
                            .build());
            LOGGER.error("Adding geofences failed", e);
        }
    }

    private GeofencingRequest buildGeofencingRequest(List<Workplace> workplaces) {
        List<Geofence> geofences = Lists.transform(workplaces, new Function<Workplace, Geofence>() {
            @Nullable
            @Override
            public Geofence apply(Workplace workplace) {
                workplace.setRegisteredAt(DateTime.now());
                workplaceRepository.save(workplace);
                return buildGeofence(workplace);
            }
        });

        return new GeofencingRequest.Builder()
                .addGeofences(geofences)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .build();
    }

    private Geofence buildGeofence(Workplace workplace) {
        return new Geofence.Builder()
                .setRequestId(workplace.getGeofenceRequestId())
                .setCircularRegion(
                        workplace.getLat(),
                        workplace.getLon(),
                        (float) workplace.getRadius())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(180000)
                .build();
    }

    private PendingIntent buildGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofencingTransitionService_.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void logAnalyticsEvent(AnalyticsEvents event, Bundle extras) {
        FirebaseAnalytics
                .getInstance(this)
                .logEvent(event.name(), extras);
    }
}
