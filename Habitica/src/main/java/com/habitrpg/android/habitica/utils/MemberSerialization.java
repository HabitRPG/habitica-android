package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.models.members.Member;
import com.habitrpg.android.habitica.models.members.MemberPreferences;
import com.habitrpg.android.habitica.models.social.UserParty;
import com.habitrpg.android.habitica.models.user.Authentication;
import com.habitrpg.android.habitica.models.user.ContributorInfo;
import com.habitrpg.android.habitica.models.user.Inbox;
import com.habitrpg.android.habitica.models.user.Items;
import com.habitrpg.android.habitica.models.user.Outfit;
import com.habitrpg.android.habitica.models.user.Profile;
import com.habitrpg.android.habitica.models.user.Stats;

import java.lang.reflect.Type;

import io.realm.Realm;

public class MemberSerialization implements JsonDeserializer<Member> {
    @Override
    public Member deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String id = obj.get("_id").getAsString();

        Realm realm = Realm.getDefaultInstance();
        Member member = realm.where(Member.class).equalTo("id", id).findFirst();
        if (member == null) {
            member = new Member();
            member.setId(id);
        } else {
            member = realm.copyFromRealm(member);
        }

        if (obj.has("stats")) {
            member.setStats(context.deserialize(obj.get("stats"), Stats.class));
        }
        if (obj.has("inbox")) {
            member.setInbox(context.deserialize(obj.get("inbox"), Inbox.class));
        }
        if (obj.has("preferences")) {
            member.setPreferences(context.deserialize(obj.get("preferences"), MemberPreferences.class));
        }
        if (obj.has("profile")) {
            member.setProfile(context.deserialize(obj.get("profile"), Profile.class));
        }
        if (obj.has("party")) {
            member.setParty(context.deserialize(obj.get("party"), UserParty.class));
            if (member.getParty() != null && member.getParty().getQuest() != null) {
                member.getParty().getQuest().setId(member.getId());
                if (!obj.get("party").getAsJsonObject().get("quest").getAsJsonObject().has("RSVPNeeded")) {
                    Quest quest = realm.where(Quest.class).equalTo("id", member.getId()).findFirst();
                    if (quest != null && quest.isValid()) {
                        member.getParty().getQuest().setRSVPNeeded(quest.getRSVPNeeded());
                    }
                }
            }
        }

        if (obj.has("items")) {
            JsonObject items = obj.getAsJsonObject("items");
            if (items.has("currentMount") && items.get("currentMount").isJsonPrimitive()) {
                member.setCurrentMount(items.get("currentMount").getAsString());
            }
            if (items.has("currentPet") && items.get("currentPet").isJsonPrimitive()) {
                member.setCurrentPet(items.get("currentPet").getAsString());
            }
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
        if (obj.has("contributor")) {
            member.setContributor(context.deserialize(obj.get("contributor"), ContributorInfo.class));
        }
        if (obj.has("auth")) {
            member.setAuthentication(context.deserialize(obj.get("auth"), Authentication.class));
        }
        if (obj.has("loginIncentives")) {
            member.setLoginIncentives(obj.get("loginIncentives").getAsInt());
        }

        /*
        TODO: Fix ownership storage
        Right now ownership is a boolean field on the item/pet/mount/equipment itself.
        Storing this data for the user can overwrite the ownership for the logged in user. The fix
        is to properly store ownership of these things in a different object, similar to how the
        iOS app handles it.
        if (obj.has("items")) {
            member.setItems(context.deserialize(obj.get("items"), Items.class));
        }
        */
        member.setId(member.getId());

        realm.close();
        return member;
    }
}
