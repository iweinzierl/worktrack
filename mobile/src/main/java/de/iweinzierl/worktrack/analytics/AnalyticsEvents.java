package de.iweinzierl.worktrack.analytics;

public enum AnalyticsEvents {

    ACTIVITY_RESUMED,
    ACTIVITY_PAUSED,

    GOOGLE_API_CONNECT_FAILURE,

    TRACKING_ITEM_SAVE,
    TRACKING_ITEM_SAVE_DUPLICATE,
    TRACKING_ITEM_SAVE_SUCCESS,
    TRACKING_ITEM_SAVE_FAILURE,
    TRACKING_ITEM_DELETE,
    TRACKING_ITEM_DELETE_SUCCESS,
    TRACKING_ITEM_DELETE_FAILURE
}
