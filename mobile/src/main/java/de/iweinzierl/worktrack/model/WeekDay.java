package de.iweinzierl.worktrack.model;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.iweinzierl.worktrack.persistence.TrackingItem;

public class WeekDay {

    private final LocalDate date;
    private final List<TrackingItem> items;

    public WeekDay(LocalDate date, List<TrackingItem> items) {
        this.date = date;
        this.items = items;
        sortItems();
    }

    public LocalDate getDate() {
        return date;
    }

    public List<TrackingItem> getItems() {
        return items;
    }

    public Duration getWorkingTime() {
        Duration duration = new Duration(0);

        for (int idx = 0; idx < items.size(); idx += 2) {
            if (idx < items.size() - 1) {
                DateTime tA = items.get(idx).getEventTime();
                DateTime tB = items.get(idx + 1).getEventTime();

                long diff = tB.getMillis() - tA.getMillis();
                duration = duration.plus(diff);
            }
        }

        return duration;
    }

    private void sortItems() {
        Collections.sort(items, new Comparator<TrackingItem>() {
            @Override
            public int compare(TrackingItem trackingItem, TrackingItem t1) {
                return trackingItem.getEventTime().compareTo(t1.getEventTime());
            }
        });
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private LocalDate localDate;
        private List<TrackingItem> items;

        public Builder() {
            items = Lists.newArrayList();
        }

        public Builder withLocalDate(LocalDate localDate) {
            this.localDate = localDate;
            return this;
        }

        public Builder withItem(TrackingItem item) {
            items.add(item);
            return this;
        }

        public Builder withItems(List<TrackingItem> items) {
            this.items.addAll(items);
            return this;
        }

        public WeekDay build() {
            filterItems();

            return new WeekDay(localDate, items);
        }

        private void filterItems() {
            this.items = Lists.newArrayList(Collections2.filter(items, new Predicate<TrackingItem>() {
                @Override
                public boolean apply(TrackingItem input) {
                    return input.getEventTime().toLocalDate().equals(localDate);
                }
            }));
        }
    }
}
