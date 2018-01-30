package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.Tag;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

public class TaskTagDeserializer implements JsonDeserializer<List<Tag>> {
    @Override
    public List<Tag> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RealmList<Tag> tags = new RealmList<>();
        List<Tag> databaseTags;
        try {
            Realm realm = Realm.getDefaultInstance();
            databaseTags = realm.copyFromRealm(realm.where(Tag.class).findAll());
            realm.close();
        } catch (RuntimeException e) {
            //Tests don't have a database
            databaseTags = new ArrayList<>();
        }

        if (json.isJsonArray()) {
            for (JsonElement tagElement : json.getAsJsonArray()) {
                if (tagElement.isJsonObject()) {
                    tags.add(context.deserialize(tagElement, Tag.class));
                } else {
                    try {
                        String tagId = tagElement.getAsString();
                        for (Tag tag : databaseTags) {
                            if (tag.getId().equals(tagId)) {
                                if (!alreadyContainsTag(tags, tagId)) {
                                    tags.add(tag);
                                }

                                break;
                            }
                        }
                    } catch (UnsupportedOperationException ignored) {

                    }
                }

            }
        }

        return tags;
    }

    private boolean alreadyContainsTag(List<Tag> list, String idToCheck) {
        for (Tag t : list) {
            if (t.getId().equals(idToCheck)) {
                return true;
            }
        }

        return false;
    }
}
