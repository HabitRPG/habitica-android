package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.Mount;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;

public class MountMapDeserializer implements JsonDeserializer<Map<String, Mount>> {
    @Override
    public Map<String, Mount> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        HashMap<String, Mount> vals = new HashMap<>();
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();

            Realm realm = Realm.getDefaultInstance();
            List<Mount> existingItems = realm.copyFromRealm(realm.where(Mount.class).findAll());
            realm.close();

            for (Mount mount : existingItems) {
                if (object.has(mount.getKey())) {
                    vals.put(mount.getKey(), mount);
                    object.remove(mount.getKey());
                }
            }

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                Mount mount;
                mount = new Mount();
                mount.setKey(entry.getKey());
                mount.setAnimal(entry.getKey().split("-")[0]);
                mount.setColor(entry.getKey().split("-")[1]);
                vals.put(mount.getKey(), mount);
            }
        }

        return vals;
    }
}
