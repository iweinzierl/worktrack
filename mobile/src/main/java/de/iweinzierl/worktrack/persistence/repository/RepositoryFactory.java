package de.iweinzierl.worktrack.persistence.repository;

import android.content.Context;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import de.iweinzierl.worktrack.WorktrackApplication;
import de.iweinzierl.worktrack.persistence.repository.free.FreeTrackingItemRepository_;

@EBean
public class RepositoryFactory {

    @RootContext
    protected Context context;

    @App
    protected WorktrackApplication application;

    private WorkplaceRepository workplaceRepository;
    private TrackingItemRepository trackingItemRepository;

    @AfterInject
    void init() {
        workplaceRepository = LocalWorkplaceRepository_.getInstance_(context);

        if (application.isPro()) {
            trackingItemRepository = LocalTrackingItemRepository_.getInstance_(context);
        } else {
            trackingItemRepository = FreeTrackingItemRepository_.getInstance_(context);
        }
    }

    public WorkplaceRepository getWorkplaceRepository() {
        return workplaceRepository;
    }

    public TrackingItemRepository getTrackingItemRepository() {
        return trackingItemRepository;
    }
}
