package de.iweinzierl.worktrack.util;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.StringWriter;

import au.com.bytecode.opencsv.CSVWriter;
import de.iweinzierl.worktrack.model.Week;
import de.iweinzierl.worktrack.model.WeekDay;
import de.iweinzierl.worktrack.model.Year;
import de.iweinzierl.worktrack.persistence.CreationType;
import de.iweinzierl.worktrack.persistence.TrackingItem;
import de.iweinzierl.worktrack.persistence.TrackingItemType;

public class CsvTransformer {

    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    private static final PeriodFormatter PERIOD_FORMAT = new PeriodFormatterBuilder()
            .appendHours()
            .appendSeparatorIfFieldsAfter(":")
            .appendMinutes()
            .toFormatter();

    private final String datePattern;
    private final String dateTimePattern;

    private StringWriter writer;
    private CSVWriter csvWriter;

    public CsvTransformer() {
        this(DEFAULT_DATE_PATTERN, DEFAULT_DATE_TIME_PATTERN);
    }

    public CsvTransformer(String datePattern, String dateTimePattern) {
        this.datePattern = datePattern;
        this.dateTimePattern = dateTimePattern;
    }

    private void initialize() {
        writer = new StringWriter();
        csvWriter = new CSVWriter(writer, ',', '"');
    }

    public String[] toStringArray(TrackingItem item) {
        return new String[]{
                String.valueOf(item.getId()),
                String.valueOf(item.getBackendId()),
                item.getType().name(),
                item.getCreationType().name(),
                item.getEventTime().toString(dateTimePattern)
        };
    }

    public TrackingItem fromArrayString(String[] columns) {
        return new TrackingItem(
                Long.valueOf(columns[0]),
                Long.valueOf(columns[1]),
                TrackingItemType.valueOf(columns[2]),
                CreationType.valueOf(columns[3]),
                DateTime.parse(columns[4], DateTimeFormat.forPattern(dateTimePattern))
        );
    }

    public String transform(Week week) {
        initialize();

        appendWeek(week);

        return writer.toString();
    }

    public String transform(Year year) {
        initialize();

        for (Week week : year.getWeeks()) {
            appendWeek(week);
        }

        return writer.toString();
    }

    private void appendWeek(Week week) {
        for (WeekDay day : week.getDays()) {
            appendDay(day);
        }
    }

    private void appendDay(WeekDay day) {
        for (int i = 0; i < day.getItems().size(); i = i + 2) {
            TrackingItem itemA = day.getItems().get(i);
            TrackingItem itemB = day.getItems().get(i + 1);

            Period period = new Period(
                    itemA.getEventTime().getMillis(),
                    itemB.getEventTime().getMillis());

            csvWriter.writeNext(new String[]{
                    itemA.getEventTime().toString(datePattern),
                    itemA.getCreationType().name(),
                    itemA.getEventTime().toString(dateTimePattern),
                    itemB.getCreationType().name(),
                    itemB.getEventTime().toString(dateTimePattern),
                    PERIOD_FORMAT.print(period)
            });
        }
    }
}
