package de.iweinzierl.worktrack.persistence.converter;

import org.greenrobot.greendao.converter.PropertyConverter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class DateTimeConverter implements PropertyConverter<DateTime, String> {

    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    @Override
    public DateTime convertToEntityProperty(String databaseValue) {
        return databaseValue == null
                ? null
                : DateTime.parse(databaseValue, DateTimeFormat.forPattern(DATETIME_FORMAT));
    }

    @Override
    public String convertToDatabaseValue(DateTime entityProperty) {
        return entityProperty == null
                ? null
                : entityProperty.toString(DATETIME_FORMAT);
    }
}
