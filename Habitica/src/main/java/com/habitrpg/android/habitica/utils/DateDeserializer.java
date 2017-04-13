package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by viirus on 21/01/16.
 */
public class DateDeserializer implements JsonDeserializer<Date>, JsonSerializer<Date> {

    private final DateFormat dateFormat;
    private final DateFormat alternativeFormat;

    public DateDeserializer() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        alternativeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        alternativeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public synchronized Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        if (jsonElement.getAsString().length() == 0) {
            return null;
        }
        try {
            return dateFormat.parse(jsonElement.getAsString());
        } catch (ParseException e) {
            try {
                return alternativeFormat.parse(jsonElement.getAsString());
            } catch (ParseException e1) {
                try {
                    Long timestamp = jsonElement.getAsLong();
                    if (timestamp > 0) {
                        return new Date(timestamp);
                    } else {
                        return null;
                    }
                } catch (NumberFormatException e2) {
                    return null;
                }
            }
        }
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return new JsonPrimitive("");
        }
        return new JsonPrimitive(this.dateFormat.format(src));
    }
}