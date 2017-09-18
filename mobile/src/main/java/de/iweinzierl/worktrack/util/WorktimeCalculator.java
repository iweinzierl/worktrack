package de.iweinzierl.worktrack.util;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.api.client.util.Lists;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemType;

public class WorktimeCalculator {

    private class EventTimeComparator implements Comparator<TrackingItem> {
        @Override
        public int compare(TrackingItem a, TrackingItem b) {
            return a.getEventTime().compareTo(b.getEventTime());
        }
    }

    private class ClusterItem {
        TrackingItem checkIn;
        TrackingItem checkOut;
    }

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("WorkingtimeCalculator");

    private final List<TrackingItem> trackingItems;
    private final Comparator<TrackingItem> comparator;

    public WorktimeCalculator(List<TrackingItem> trackingItems) {
        this.trackingItems = trackingItems;
        this.comparator = new EventTimeComparator();
    }

    public Period calculate() {
        clusterItems();

        Period duration = new Period();

        List<ClusterItem> items = clusterItems();

        if (items.isEmpty()) {
            return new Period();
        }

        for (ClusterItem item : items) {
            duration = duration.plus(calculate(item));
        }

        return duration;
    }

    private List<ClusterItem> clusterItems() {
        if (trackingItems == null || trackingItems.isEmpty()) {
            return Lists.newArrayList();
        }

        Collections.sort(trackingItems, comparator);

        List<ClusterItem> clusterItems = Lists.newArrayList();
        ClusterItem current = new ClusterItem();

        for (TrackingItem trackingItem : trackingItems) {
            TrackingItemType type = trackingItem.getType();

            if (current.checkIn == null && type == TrackingItemType.CHECKIN) {
                current.checkIn = trackingItem;
            } else if (current.checkIn != null && current.checkOut == null && type == TrackingItemType.CHECKOUT) {
                current.checkOut = trackingItem;
            } else {
                LOGGER.warn("Undefined state -> checkIn = {}  & checkOut = {}", current.checkIn, current.checkOut);
            }

            if (current.checkIn != null && current.checkOut != null) {
                clusterItems.add(current);
                current = new ClusterItem();
            }
        }

        if (current.checkIn != null && current.checkOut == null) {
            clusterItems.add(current);
        }

        return clusterItems;
    }

    private ReadablePeriod calculate(ClusterItem item) {
        DateTime tA = item.checkIn.getEventTime();
        DateTime tB = item.checkOut == null
                ? DateTime.now()
                : item.checkOut.getEventTime();

        return new Period(tA, tB);
    }
}
