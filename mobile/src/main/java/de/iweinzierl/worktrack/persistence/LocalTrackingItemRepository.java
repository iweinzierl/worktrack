package de.iweinzierl.worktrack.persistence;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.greenrobot.greendao.query.WhereCondition;
import org.joda.time.LocalDate;
import org.slf4j.Logger;

import java.util.List;

import de.iweinzierl.worktrack.model.Week;
import de.iweinzierl.worktrack.model.WeekDay;
import de.iweinzierl.worktrack.model.Year;
import de.iweinzierl.worktrack.persistence.converter.DateTimeConverter;

@EBean
public class LocalTrackingItemRepository implements TrackingItemRepository {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("LocalTrackingItemRepository");

    @Bean
    protected DaoSessionFactory sessionFactory;

    @Override
    public TrackingItem save(TrackingItem item) {
        try {
            sessionFactory.getSession().getTrackingItemDao().save(item);
            return item;
        } catch (Exception e) {
            LOGGER.error("Saving tracking item failed", e);
        }

        return null;
    }

    @Override
    public boolean delete(TrackingItem item) {
        try {
            sessionFactory.getSession().getTrackingItemDao().delete(item);
            return true;
        } catch (Exception e) {
            LOGGER.error("Deleting tracking item failed", e);
        }

        return false;
    }

    @Override
    public boolean deleteAll() {
        try {
            sessionFactory.getSession().getTrackingItemDao().deleteAll();
            return true;
        } catch (Exception e) {
            LOGGER.error("Deleting all tracking items failed", e);
        }

        return false;
    }

    @Override
    public TrackingItem update(TrackingItem item) {
        try {
            sessionFactory.getSession().getTrackingItemDao().update(item);
            return item;
        } catch (Exception e) {
            LOGGER.error("Deleting tracking item failed", e);
        }

        return null;
    }

    @Override
    public TrackingItem findById(long id) {
        try {
            return sessionFactory.getSession().getTrackingItemDao().load(id);
        } catch (Exception e) {
            LOGGER.error("Deleting tracking item failed", e);
        }

        return null;
    }

    @Override
    public List<TrackingItem> findAll() {
        try {
            return sessionFactory.getSession().getTrackingItemDao().loadAll();
        } catch (Exception e) {
            LOGGER.error("Deleting tracking item failed", e);
        }

        return null;
    }

    @Override
    public List<TrackingItem> findByDate(final LocalDate date) {
        try {
            WhereCondition where = new WhereCondition.StringCondition(
                    "substr(T.\"" + TrackingItemDao.Properties.EventTime.columnName + "\", 1, 10)"
                            + " = '" + date.toString(DateTimeConverter.DATE_FORMAT) + "'");

            return sessionFactory.getSession().queryBuilder(TrackingItem.class)
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
            TrackingItem first = sessionFactory.getSession().queryBuilder(TrackingItem.class)
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

            return sessionFactory.getSession().queryBuilder(TrackingItem.class)
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
