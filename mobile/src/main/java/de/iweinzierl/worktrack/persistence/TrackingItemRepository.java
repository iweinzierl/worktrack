package de.iweinzierl.worktrack.persistence;

import org.joda.time.LocalDate;

import java.util.List;

import de.iweinzierl.worktrack.model.Week;
import de.iweinzierl.worktrack.model.Year;

public interface TrackingItemRepository {

    TrackingItem save(TrackingItem item);

    boolean delete(TrackingItem item);

    boolean deleteAll();

    TrackingItem update(TrackingItem item);

    TrackingItem findById(long id);

    List<TrackingItem> findAll();

    List<TrackingItem> findByDate(LocalDate date);

    LocalDate findFirstLocalDate();

    Week findWeek(int year, int weekNum);

    Year findYear(int year);

    boolean hasItemsAt(LocalDate date);
}
