package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.models.members.Member;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.user.User;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class GroupSerialization implements JsonDeserializer<Group>, JsonSerializer<Group> {
    @Override
    public Group deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Group group = new Group();
        JsonObject obj = json.getAsJsonObject();
        group.id = obj.get("_id").getAsString();
        group.name = obj.get("name").getAsString();
        if (obj.has("description") && !obj.get("description").isJsonNull()) {
            group.description = obj.get("description").getAsString();
        }
        if (obj.has("leaderMessage") && !obj.get("leaderMessage").isJsonNull()) {
            group.leaderMessage = obj.get("leaderMessage").getAsString();
        }
        if (obj.has("privacy")) {
            group.privacy = obj.get("privacy").getAsString();
        }
        if (obj.has("memberCount")) {
            group.memberCount = obj.get("memberCount").getAsInt();
        }
        if (obj.has("balance")) {
            group.balance = obj.get("balance").getAsDouble();
        }
        if (obj.has("logo") && !obj.get("logo").isJsonNull()) {
            group.logo = obj.get("logo").getAsString();
        }
        if (obj.has("type")) {
            group.type = obj.get("type").getAsString();
        }
        if (obj.has("chat")) {
            group.chat = context.deserialize(obj.get("chat"), new TypeToken<RealmList<ChatMessage>>() {
            }.getType());
        }
        if (obj.has("leader")) {
            if (obj.get("leader").isJsonPrimitive()) {
                group.leaderID = obj.get("leader").getAsString();
            } else {
                JsonObject leader = obj.get("leader").getAsJsonObject();
                group.leaderID = leader.get("_id").getAsString();
                if (leader.has("profile") && !leader.get("profile").isJsonNull()) {
                    if (leader.get("profile").getAsJsonObject().has("name"))
                        group.leaderName = leader.get("profile").getAsJsonObject().get("name").getAsString();
                }
            }
        }
        if (obj.has("quest")) {
            group.quest = context.deserialize(obj.get("quest"), new TypeToken<Quest>() {
            }.getType());
            group.quest.id = group.id;

            if (obj.getAsJsonObject("quest").has("members")) {
                JsonObject members = obj.getAsJsonObject("quest").getAsJsonObject("members");
                Realm realm = Realm.getDefaultInstance();
                List<Member> dbMembers = realm.copyFromRealm(realm.where(Member.class).equalTo("party.id", group.id).findAll());
                realm.close();
                for (Member member : dbMembers) {
                    if (members.has(member.getId())) {
                        JsonElement value = members.get(member.getId());
                        if (value.isJsonNull()) {
                            member.setParticipatesInQuest(null);
                        } else {
                            member.setParticipatesInQuest(value.getAsBoolean());
                        }
                    } else {
                        member.setParticipatesInQuest(null);
                    }
                    members.remove(member.getId());
                }
                for (Map.Entry<String, JsonElement> entry : members.entrySet()) {
                    Member member = new Member();
                    member.setId(entry.getKey());
                    if (!entry.getValue().isJsonNull()) {
                        member.setParticipatesInQuest(entry.getValue().getAsBoolean());
                    }
                    dbMembers.add(member);
                }
                RealmList<Member> newMembers = new RealmList<>();
                newMembers.addAll(dbMembers);
                group.quest.participants = newMembers;
            }
        }

        return group;
    }

    @Override
    public JsonElement serialize(Group src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", src.name);
        obj.addProperty("description", src.description);
        obj.addProperty("logo", src.logo);
        obj.addProperty("type", src.type);
        obj.addProperty("type", src.type);
        obj.addProperty("leader", src.leaderID);
        return obj;
    }
}
