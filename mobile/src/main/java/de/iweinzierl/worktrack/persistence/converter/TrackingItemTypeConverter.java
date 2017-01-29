package de.iweinzierl.worktrack.persistence.converter;

import org.greenrobot.greendao.converter.PropertyConverter;

import de.iweinzierl.worktrack.persistence.TrackingItemType;

public class TrackingItemTypeConverter implements PropertyConverter<TrackingItemType, Integer> {

    @Override
    public TrackingItemType convertToEntityProperty(Integer databaseValue) {
        return databaseValue == null
                ? null
                : TrackingItemType.byId(databaseValue);
    }

    @Override
    public Integer convertToDatabaseValue(TrackingItemType entityProperty) {
        return entityProperty == null
                ? null
                : entityProperty.id;
    }
}
