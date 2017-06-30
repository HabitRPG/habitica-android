package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.models.members.Member;
import com.habitrpg.android.habitica.models.social.UserParty;
import com.habitrpg.android.habitica.models.user.ContributorInfo;
import com.habitrpg.android.habitica.models.user.Flags;
import com.habitrpg.android.habitica.models.user.Inbox;
import com.habitrpg.android.habitica.models.user.Outfit;
import com.habitrpg.android.habitica.models.user.Preferences;
import com.habitrpg.android.habitica.models.user.Profile;
import com.habitrpg.android.habitica.models.user.Stats;

import java.lang.reflect.Type;

import io.realm.Realm;

public class MemberSerialization implements JsonDeserializer<Member> {
    @Override
    public Member deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Member member = new Member();
        JsonObject obj = json.getAsJsonObject();

        if (obj.has("_id")) {
            member.setId(obj.get("_id").getAsString());
        }

        if (obj.has("stats")) {
            member.setStats(context.deserialize(obj.get("stats"), Stats.class));
        }
        if (obj.has("inbox")) {
            member.setInbox(context.deserialize(obj.get("inbox"), Inbox.class));
        }
        if (obj.has("preferences")) {
            member.setPreferences(context.deserialize(obj.get("preferences"), Preferences.class));
        }
        if (obj.has("profile")) {
            member.setProfile(context.deserialize(obj.get("profile"), Profile.class));
        }
        if (obj.has("party")) {
            member.setParty(context.deserialize(obj.get("party"), UserParty.class));
            if (member.getParty() != null && member.getParty().getQuest() != null) {
                member.getParty().getQuest().id = member.getId();
                if (!obj.get("party").getAsJsonObject().get("quest").getAsJsonObject().has("RSVPNeeded")) {
                    Realm realm = Realm.getDefaultInstance();
                    Quest quest = realm.where(Quest.class).equalTo("id", member.getId()).findFirst();
                    if (quest != null && quest.isValid()) {
                        member.getParty().getQuest().RSVPNeeded = quest.RSVPNeeded;
                    }
                }
            }
        }

        if (obj.has("items")) {
            JsonObject items = obj.getAsJsonObject("items");
            if (items.has("gear")) {
                JsonObject gear = items.getAsJsonObject("gear");
                if (gear.has("costume")) {
                    member.setCostume(context.deserialize(gear.get("costume"), Outfit.class));
                }
                if (gear.has("equipped")) {
                    member.setEquipped(context.deserialize(gear.get("equipped"), Outfit.class));
                }
            }
        }
        if (obj.has("flags")) {
            member.setFlags(context.deserialize(obj.get("flags"), Flags.class));
        }
        if (obj.has("contributor")) {
            member.setContributor(context.deserialize(obj.get("contributor"), ContributorInfo.class));
        }

        return member;
    }
}
