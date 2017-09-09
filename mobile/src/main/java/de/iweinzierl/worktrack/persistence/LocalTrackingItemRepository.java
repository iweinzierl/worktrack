package de.iweinzierl.worktrack.persistence;

import android.content.Context;
import android.os.Bundle;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.greenrobot.greendao.query.WhereCondition;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;

import java.util.List;

import de.iweinzierl.worktrack.analytics.AnalyticsEvents;
import de.iweinzierl.worktrack.analytics.AnalyticsParams;
import de.iweinzierl.worktrack.model.Week;
import de.iweinzierl.worktrack.model.WeekDay;
import de.iweinzierl.worktrack.model.Year;
import de.iweinzierl.worktrack.persistence.converter.DateTimeConverter;

@EBean
public class LocalTrackingItemRepository implements TrackingItemRepository {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("LocalTrackingItemRepository");

    private static final String DATETIME_FORMAT_NO_SECONDS = "yyyy-MM-dd HH:mm";

    @RootContext
    protected Context context;

    @Bean
    DaoSessionFactory sessionFactory;

    private DaoSession session;
    private FirebaseAnalytics analytics;

    public DaoSession getSession() {
        if (session == null) {
            session = sessionFactory.getSession();
        }

        return session;
    }

    public FirebaseAnalytics getAnalytics() {
        if (analytics == null) {
            analytics = FirebaseAnalytics.getInstance(context);
        }

        return analytics;
    }

    @Override
    public TrackingItem save(TrackingItem item) {
        getAnalytics().logEvent(AnalyticsEvents.TRACKING_ITEM_SAVE.name(), null);

        try {
            TrackingItemDao trackingItemDao = getSession().getTrackingItemDao();
            String eventTime = item.getEventTime().toString(DATETIME_FORMAT_NO_SECONDS) + "%";

            String where = "WHERE " +
                    TrackingItemDao.Properties.Type.columnName + " = " + item.getType().id + " AND " +
                    TrackingItemDao.Properties.EventTime.columnName + " LIKE ?";

            List<TrackingItem> trackingItems = trackingItemDao.queryRaw(where, eventTime);

            if (trackingItems == null || trackingItems.isEmpty()) {
                trackingItemDao.save(item);
                getAnalytics().logEvent(AnalyticsEvents.TRACKING_ITEM_SAVE_SUCCESS.name(), null);
            } else {
                getAnalytics().logEvent(AnalyticsEvents.TRACKING_ITEM_SAVE_DUPLICATE.name(), null);
            }

            return item;
        } catch (Exception e) {
            LOGGER.error("Saving tracking item failed", e);

            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsParams.ERROR_MESSAGE.name(), e.getMessage());
            getAnalytics().logEvent(AnalyticsEvents.TRACKING_ITEM_SAVE_FAILURE.name(), bundle);
        }

        return null;
    }

    @Override
    public boolean delete(TrackingItem item) {
        getAnalytics().logEvent(AnalyticsEvents.TRACKING_ITEM_DELETE.name(), null);

        try {
            getSession().getTrackingItemDao().delete(item);
            getAnalytics().logEvent(AnalyticsEvents.TRACKING_ITEM_DELETE_SUCCESS.name(), null);
            return true;
        } catch (Exception e) {
            LOGGER.error("Deleting tracking item failed", e);

            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsParams.ERROR_MESSAGE.name(), e.getMessage());
            getAnalytics().logEvent(AnalyticsEvents.TRACKING_ITEM_DELETE_FAILURE.name(), bundle);
        }

        return false;
    }

    @Override
    public void deleteByEventTime(DateTime eventTime) {
        List<TrackingItem> items = findByEventTime(eventTime);

        for (TrackingItem item : items) {
            delete(item);
        }
    }

    @Override
    public boolean deleteAll() {
        try {
            getSession().getTrackingItemDao().deleteAll();
            return true;
        } catch (Exception e) {
            LOGGER.error("Deleting all tracking items failed", e);
        }

        return false;
    }

    @Override
    public TrackingItem update(TrackingItem item) {
        try {
            getSession().getTrackingItemDao().update(item);
            return item;
        } catch (Exception e) {
            LOGGER.error("Deleting tracking item failed", e);
        }

        return null;
    }

    @Override
    public TrackingItem findById(long id) {
        try {
            return getSession().getTrackingItemDao().load(id);
        } catch (Exception e) {
            LOGGER.error("Deleting tracking item failed", e);
        }

        return null;
    }

    @Override
    public List<TrackingItem> findAll() {
        try {
            return getSession().getTrackingItemDao().loadAll();
        } catch (Exception e) {
            LOGGER.error("Finding all tracking items failed", e);
        }

        return null;
    }

    @Override
    public List<TrackingItem> findByEventTime(DateTime eventTime) {
        return getSession().queryBuilder(TrackingItem.class)
                .where(TrackingItemDao.Properties.EventTime.eq(
                        eventTime.toString(DateTimeConverter.DATETIME_FORMAT)
                ))
                .build()
                .list();
    }

    @Override
    public List<TrackingItem> findByDate(final LocalDate date) {
        try {
            WhereCondition where = new WhereCondition.StringCondition(
                    "substr(T.\"" + TrackingItemDao.Properties.EventTime.columnName + "\", 1, 10)"
                            + " = '" + date.toString(DateTimeConverter.DATE_FORMAT) + "'");

            return getSession().queryBuilder(TrackingItem.class)
                    .where(where)
                    .orderAsc(TrackingItemDao.Properties.EventTime)
                    .list();

        } catch (Exception e) {
            LOGGER.error("Deleting tracking item failed", e);
        }

        return null;
    }

    @Override
    public LocalDate findFirstLocalDate() {
        try {
            TrackingItem first = getSession().queryBuilder(TrackingItem.class)
                    .limit(1)
                    .orderAsc(TrackingItemDao.Properties.EventTime)
                    .unique();

            return first == null ? null : first.getEventTime().toLocalDate();

        } catch (Exception e) {
            LOGGER.error("Problem while fining first local date", e);
        }

        return null;
    }

    @Override
    public Week findWeek(int year, int weekNum) {
        LocalDate minDate = LocalDate.now()
                .withYear(year)
                .withWeekOfWeekyear(weekNum)
                .withDayOfWeek(1);

        Week.Builder builder = Week.newBuilder()
                .withYear(year)
                .withWeekNum(weekNum);

        for (int i = 0; i < 7; i++) {
            List<TrackingItem> items = findByDate(minDate.plusDays(i));
            builder.withWeekDay(WeekDay.newBuilder()
                    .withItems(items)
                    .withLocalDate(minDate.plusDays(i))
                    .build());
        }

        return builder.build();
    }

    @Override
    public Year findYear(int year) {
        Year.Builder builder = Year.newBuilder().withYear(year);

        for (int week = 1; week <= 52; week++) {
            builder.withWeek(findWeek(year, week));
        }

        return builder.build();
    }

    @Override
    public boolean hasItemsAt(LocalDate date) {
        if (date == null) {
            return false;
        }

        try {
            WhereCondition where = new WhereCondition.StringCondition(
                    "substr(T.\"" + TrackingItemDao.Properties.EventTime.columnName + "\", 1, 10)"
                            + " = '" + date.toString(DateTimeConverter.DATE_FORMAT) + "'");

            return getSession().queryBuilder(TrackingItem.class)
                    .where(where)
                    .orderAsc(TrackingItemDao.Properties.EventTime)
                    .limit(1)
                    .unique() != null;
        } catch (Exception e) {
            LOGGER.error("Problem while determining if items exist for given date.", e);
        }

        return false;
    }
}
