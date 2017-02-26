package de.iweinzierl.worktrack.model;

import com.google.common.collect.Lists;

import org.joda.time.Duration;

import java.util.List;

public class Year {

    private final int year;
    private final List<Week> weeks;

    public Year(int year, List<Week> weeks) {
        this.year = year;
        this.weeks = weeks;
    }

    public int getYear() {
        return year;
    }

    public List<Week> getWeeks() {
        return weeks;
    }

    public Duration getWorkingTime() {
        Duration duration = new Duration(0);

        for (Week week : weeks) {
            duration = duration.plus(week.getWorkingTime());
        }

        return duration;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int year;
        private List<Week> weeks;

        public Builder() {
            weeks = Lists.newArrayList();
        }

        public int getYear() {
            return year;
        }

        public Builder withYear(int year) {
            this.year = year;
            return this;
        }

        public Builder withWeek(Week week) {
            this.weeks.add(week);
            return this;
        }

        public Year build() {
            return new Year(year, weeks);
        }
    }
}
