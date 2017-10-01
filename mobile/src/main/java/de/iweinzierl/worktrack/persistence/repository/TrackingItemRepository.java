package de.iweinzierl.worktrack.persistence.repository;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.List;

import de.iweinzierl.worktrack.model.Week;
import de.iweinzierl.worktrack.model.Year;
import de.iweinzierl.worktrack.persistence.DaoSession;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.repository.exception.PersistenceException;

public interface TrackingItemRepository {

    TrackingItem save(TrackingItem item) throws PersistenceException;

    boolean delete(TrackingItem item);

    void deleteByEventTime(DateTime eventTime);

    boolean deleteAll();

    TrackingItem update(TrackingItem item);

    TrackingItem findById(long id);

    List<TrackingItem> findByEventTime(DateTime eventTime);

    List<TrackingItem> findAll();

    List<TrackingItem> findByDate(LocalDate date);

    LocalDate findFirstLocalDate();

    Week findWeek(int year, int weekNum);

    Year findYear(int year);

    boolean hasItemsAt(LocalDate date);
}
