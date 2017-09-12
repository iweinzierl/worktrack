package de.iweinzierl.worktrack.util.gson;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;

public class DateTimeConverter implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final DateTimeFormatter DATETIME_FORMAT = DateTimeFormat.forPattern(DATETIME_PATTERN);

    @Override
    public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ? null : new JsonPrimitive(src.toString(DATETIME_FORMAT));
    }

    @Override
    public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return json != null && !Strings.isNullOrEmpty(json.getAsString())
                ? DATETIME_FORMAT.parseDateTime(json.getAsString())
                : null;
    }
}
