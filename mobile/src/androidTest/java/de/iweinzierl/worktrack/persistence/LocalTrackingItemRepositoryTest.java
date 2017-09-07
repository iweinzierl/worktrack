package de.iweinzierl.worktrack.persistence;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(AndroidJUnit4.class)
public class LocalTrackingItemRepositoryTest {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Mock
    Context context;

    private LocalTrackingItemRepository trackingItemRepository;

    @Before
    public void setup() {
        trackingItemRepository = LocalTrackingItemRepository_.getInstance_(context);
        trackingItemRepository.deleteAll();
    }

    @Test
    public void save() throws Exception {
        TrackingItem trackingItem = trackingItemRepository.save(new TrackingItem(
                TrackingItemType.CHECKIN,
                DateTime.parse("2017-09-07 20:01:05", DATETIME_FORMATTER),
                CreationType.AUTO
        ));

        Assert.assertNotNull("TrackingItem was not saved, ID not set", trackingItem.getId());
    }

    @Test
    public void saveNoDuplicate() throws Exception {
        TrackingItem trackingItem = trackingItemRepository.save(new TrackingItem(
                TrackingItemType.CHECKIN,
                DateTime.parse("2017-09-07 20:01:05", DATETIME_FORMATTER),
                CreationType.AUTO
        ));

        Assert.assertNotNull(trackingItem.getId());

        TrackingItem duplicate = trackingItemRepository.save(new TrackingItem(
                TrackingItemType.CHECKIN,
                DateTime.parse("2017-09-07 20:01:44", DATETIME_FORMATTER),
                CreationType.AUTO
        ));

        Assert.assertNull("TrackingItem saved although it has the same type and time (excluding seconds)",
                duplicate.getId());

        TrackingItem noDuplicate = trackingItemRepository.save(new TrackingItem(
                TrackingItemType.CHECKOUT,
                DateTime.parse("2017-09-07 20:01:49", DATETIME_FORMATTER),
                CreationType.AUTO
        ));

        Assert.assertNotNull("TrackingItem not saved although it has a different type",
                noDuplicate.getId());
    }
}