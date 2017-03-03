package de.iweinzierl.worktrack.util;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.StringWriter;

import au.com.bytecode.opencsv.CSVWriter;
import de.iweinzierl.worktrack.model.Week;
import de.iweinzierl.worktrack.model.WeekDay;
import de.iweinzierl.worktrack.model.Year;
import de.iweinzierl.worktrack.persistence.TrackingItem;

public class CsvTransformer {

    private static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd";
    private static final String DEFAULT_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    private static final PeriodFormatter PERIOD_FORMAT = new PeriodFormatterBuilder()
            .appendHours()
            .appendSeparatorIfFieldsAfter(":")
            .appendMinutes()
            .toFormatter();

    private final String dateTimePattern;
    private final String timePattern;

    private StringWriter writer;
    private CSVWriter csvWriter;

    public CsvTransformer() {
        this(DEFAULT_DATETIME_PATTERN, DEFAULT_TIME_PATTERN);
    }

    public CsvTransformer(String dateTimePattern, String timePattern) {
        this.dateTimePattern = dateTimePattern;
        this.timePattern = timePattern;
    }

    private void initialize() {
        writer = new StringWriter();
        csvWriter = new CSVWriter(writer, ',', '"');
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
                    itemA.getEventTime().toString(dateTimePattern),
                    itemA.getCreationType().name(),
                    itemA.getEventTime().toString(timePattern),
                    itemB.getCreationType().name(),
                    itemB.getEventTime().toString(timePattern),
                    PERIOD_FORMAT.print(period)
            });
        }
    }
}
