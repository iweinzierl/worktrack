package de.iweinzierl.worktrack.persistence;

import org.joda.time.LocalDate;

import java.util.List;

public interface TrackingItemRepository {

    TrackingItem save(TrackingItem item);

    boolean delete(TrackingItem item);

    TrackingItem update(TrackingItem item);

    TrackingItem findById(long id);

    List<TrackingItem> findAll();

    List<TrackingItem> findByDate(LocalDate date);
}
