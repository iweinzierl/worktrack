package de.iweinzierl.worktrack.model;

import com.google.common.collect.Lists;

import org.joda.time.Duration;

import java.util.List;

public class Week {

    private final int year;
    private final int weekNum;
    private final List<WeekDay> days;

    public Week(int year, int weekNum, List<WeekDay> days) {
        this.year = year;
        this.weekNum = weekNum;
        this.days = days;
    }

    public int getYear() {
        return year;
    }

    public int getWeekNum() {
        return weekNum;
    }

    public List<WeekDay> getDays() {
        return days;
    }

    public Duration getWorkingTime() {
        Duration duration = new Duration(0);

        for (WeekDay day : days) {
            duration = duration.plus(day.getWorkingTime());
        }

        return duration;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int year;
        private int weekNum;
        private List<WeekDay> days;

        public Builder() {
            days = Lists.newArrayList();
        }

        public int getYear() {
            return year;
        }

        public int getWeekNum() {
            return weekNum;
        }

        public Builder withYear(int year) {
            this.year = year;
            return this;
        }

        public Builder withWeekNum(int weekNum) {
            this.weekNum = weekNum;
            return this;
        }

        public Builder withWeekDay(WeekDay weekDay) {
            this.days.add(weekDay);
            return this;
        }

        public Week build() {
            return new Week(year, weekNum, days);
        }
    }
}
