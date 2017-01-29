package de.iweinzierl.worktrack.persistence;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.joda.time.LocalDate;
import org.slf4j.Logger;

import java.util.List;

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
            List<TrackingItem> items = sessionFactory.getSession().getTrackingItemDao().loadAll();
            return Lists.newArrayList(Collections2.filter(items, new Predicate<TrackingItem>() {
                @Override
                public boolean apply(TrackingItem input) {
                    return input.getEventTime().equals(date);
                }
            }));
        } catch (Exception e) {
            LOGGER.error("Deleting tracking item failed", e);
        }

        return null;
    }
}
