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

public class DateDeserializer implements JsonDeserializer<Date>, JsonSerializer<Date> {

    private final DateFormat dateFormat;
    private final DateFormat alternativeFormat;
    private final DateFormat nextDueFormat;

    public DateDeserializer() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        alternativeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        alternativeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        nextDueFormat = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss zzzz", Locale.US);
        nextDueFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    }

    @Override
    public synchronized Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        if (jsonElement.isJsonArray()) {
            if (jsonElement.getAsJsonArray().size() == 0) {
                return null;
            }
            jsonElement = jsonElement.getAsJsonArray().get(0);
        }
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
                    return nextDueFormat.parse(jsonElement.getAsString());
                } catch (ParseException e2) {
                    try {
                        Long timestamp = jsonElement.getAsLong();
                        if (timestamp > 0) {
                            return new Date(timestamp);
                        } else {
                            return null;
                        }
                    } catch (NumberFormatException e3) {
                        return null;
                    }
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