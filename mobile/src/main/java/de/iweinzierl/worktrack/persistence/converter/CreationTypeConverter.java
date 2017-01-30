package de.iweinzierl.worktrack.persistence.converter;

import org.greenrobot.greendao.converter.PropertyConverter;

import de.iweinzierl.worktrack.persistence.CreationType;

public class CreationTypeConverter implements PropertyConverter<CreationType, Integer> {

    @Override
    public CreationType convertToEntityProperty(Integer databaseValue) {
        return databaseValue == null
                ? null
                : CreationType.byId(databaseValue);
    }

    @Override
    public Integer convertToDatabaseValue(CreationType entityProperty) {
        return entityProperty == null
                ? null
                : entityProperty.id;
    }
}
