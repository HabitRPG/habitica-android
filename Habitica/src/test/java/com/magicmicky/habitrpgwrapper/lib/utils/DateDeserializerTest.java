package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.habitrpg.android.habitica.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Type;
import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@Config(constants = BuildConfig.class)
@RunWith(RobolectricGradleTestRunner.class)
public class DateDeserializerTest {
    DateDeserializer deserializer;
    JsonDeserializationContext deserializationContext;
    JsonSerializationContext serializationContext;

    Long referenceTimestamp;

    @Before
    public void setup() {
        this.deserializer = new DateDeserializer();
        this.deserializationContext = new JsonDeserializationContext() {
            @Override
            public <T> T deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
                return null;
            }
        };
        this.serializationContext = new JsonSerializationContext() {
            @Override
            public JsonElement serialize(Object src) {
                return null;
            }

            @Override
            public JsonElement serialize(Object src, Type typeOfSrc) {
                return null;
            }
        };

        this.referenceTimestamp = new Long("1443445200000");
    }

    @Test
    public void validateNormalDateDeserialize() {
        Date date = this.deserializer.deserialize(new JsonPrimitive("2015-09-28T13:00:00.000Z"), Date.class, this.deserializationContext);

        assertThat(date, is(new Date(referenceTimestamp)));
    }

    @Test
    public void validateTimestampDeserialize() {
        Date date = this.deserializer.deserialize(new JsonPrimitive(referenceTimestamp), Date.class, this.deserializationContext);
        assertThat(date, is(new Date(referenceTimestamp)));
    }

    @Test
    public void validateEmptyDeserialize() {
        Date date = this.deserializer.deserialize(new JsonPrimitive(""), Date.class, this.deserializationContext);
        assertNull(date);
    }

    @Test
    public void validateNormalDateSerialize() {
        JsonElement dateElement = this.deserializer.serialize(new Date(referenceTimestamp), Date.class, this.serializationContext);
        assertThat(dateElement.getAsString(), is("2015-09-28T13:00:00.000Z"));
    }
    @Test
    public void validateEmptySerialize() {
        JsonElement dateElement = this.deserializer.serialize(null, Date.class, this.serializationContext);
        assertThat(dateElement.getAsString(), is(""));
    }
}
