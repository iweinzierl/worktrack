package de.iweinzierl.worktrack.persistence.converter;

import org.greenrobot.greendao.converter.PropertyConverter;
import org.joda.time.DateTime;

public class DateTimeConverter implements PropertyConverter<DateTime, Long> {

    @Override
    public DateTime convertToEntityProperty(Long databaseValue) {
        return databaseValue == null
                ? null
                : new DateTime(databaseValue);
    }

    @Override
    public Long convertToDatabaseValue(DateTime entityProperty) {
        return entityProperty == null
                ? null
                : entityProperty.getMillis();
    }
}
