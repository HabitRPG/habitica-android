package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.Mount;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;

public class MountListDeserializer implements JsonDeserializer<RealmList<Mount>> {

    @Override
    public RealmList<Mount> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RealmList<Mount> vals = new RealmList<>();
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();

            Realm realm = Realm.getDefaultInstance();
            List<Mount> existingItems = realm.copyFromRealm(realm.where(Mount.class).findAll());
            realm.close();

            for (Mount item : existingItems) {
                if (object.has(item.getKey())) {
                    JsonElement itemObject = object.get(item.getKey());
                    item.setOwned(itemObject.getAsBoolean());
                    vals.add(item);
                    object.remove(item.getKey());
                }
            }

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                Mount mount = new Mount();
                mount.setKey(entry.getKey());
                mount.setAnimal(entry.getKey().split("-")[0]);
                mount.setColor(entry.getKey().split("-")[1]);
                if (entry.getValue().isJsonNull()) {
                    mount.setOwned(false);
                } else {
                    mount.setOwned(entry.getValue().getAsBoolean());
                }
                vals.add(mount);
            }
        } else {
            for (JsonElement item : json.getAsJsonArray()) {
                vals.add(context.deserialize(item.getAsJsonObject(), Mount.class));
            }
        }

        return vals;
    }
}
