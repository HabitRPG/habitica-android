package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.habitrpg.android.habitica.models.PushDevice;
import com.habitrpg.android.habitica.models.Tag;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.models.invitations.Invitations;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.social.UserParty;
import com.habitrpg.android.habitica.models.tasks.TasksOrder;
import com.habitrpg.android.habitica.models.user.Authentication;
import com.habitrpg.android.habitica.models.user.ContributorInfo;
import com.habitrpg.android.habitica.models.user.Flags;
import com.habitrpg.android.habitica.models.user.Inbox;
import com.habitrpg.android.habitica.models.user.Items;
import com.habitrpg.android.habitica.models.user.Preferences;
import com.habitrpg.android.habitica.models.user.Profile;
import com.habitrpg.android.habitica.models.user.Purchases;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.models.user.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

public class UserDeserializer implements JsonDeserializer<User> {
    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        User user = new User();
        JsonObject obj = json.getAsJsonObject();

        if (obj.has("_id")) {
            user.setId(obj.get("_id").getAsString());
        }

        if (obj.has("balance")) {
            user.setBalance(obj.get("balance").getAsDouble());
        }
        if (obj.has("stats")) {
            user.setStats(context.deserialize(obj.get("stats"), Stats.class));
        }
        if (obj.has("inbox")) {
            user.setInbox(context.deserialize(obj.get("inbox"), Inbox.class));
            for (ChatMessage message : user.getInbox().getMessages()) {
                message.isInboxMessage = true;
            }
        }
        if (obj.has("preferences")) {
            user.setPreferences(context.deserialize(obj.get("preferences"), Preferences.class));
        }
        if (obj.has("profile")) {
            user.setProfile(context.deserialize(obj.get("profile"), Profile.class));
        }
        if (obj.has("party")) {
            user.setParty(context.deserialize(obj.get("party"), UserParty.class));
            if (user.getParty() != null && user.getParty().getQuest() != null) {
                user.getParty().getQuest().setId(user.getId());
                if (!obj.get("party").getAsJsonObject().get("quest").getAsJsonObject().has("RSVPNeeded")) {
                    Realm realm = Realm.getDefaultInstance();
                    Quest quest = realm.where(Quest.class).equalTo("id", user.getId()).findFirst();
                    if (quest != null && quest.isValid()) {
                        user.getParty().getQuest().setRSVPNeeded(quest.getRSVPNeeded());
                    }
                }
            }
        }

        if (obj.has("items")) {
            user.setItems(context.deserialize(obj.get("items"), Items.class));
        }
        if (obj.has("auth")) {
            user.setAuthentication(context.deserialize(obj.get("auth"), Authentication.class));
        }
        if (obj.has("flags")) {
            user.setFlags(context.deserialize(obj.get("flags"), Flags.class));
        }
        if (obj.has("contributor")) {
            user.setContributor(context.deserialize(obj.get("contributor"), ContributorInfo.class));
        }
        if (obj.has("invitations")) {
            user.setInvitations(context.deserialize(obj.get("invitations"), Invitations.class));
        }
        if (obj.has("tags")) {
            user.setTags(context.deserialize(obj.get("tags"), new TypeToken<RealmList<Tag>>() {
            }.getType()));
            for (Tag tag : user.getTags()) {
                tag.userId = user.getId();
            }
        }
        if (obj.has("tasksOrder")) {
            user.setTasksOrder(context.deserialize(obj.get("tasksOrder"), TasksOrder.class));
        }
        if (obj.has("challenges")) {
            user.setChallenges(context.deserialize(obj.get("challenges"), new TypeToken<List<Challenge>>() {
            }.getType()));
        }
        if (obj.has("purchased")) {
            user.setPurchased(context.deserialize(obj.get("purchased"), Purchases.class));
            if (obj.get("purchased").getAsJsonObject().has("plan")) {
                if (obj.get("purchased").getAsJsonObject().get("plan").getAsJsonObject().has("mysteryItems")) {
                    user.getPurchased().getPlan().mysteryItemCount = obj.get("purchased").getAsJsonObject().get("plan").getAsJsonObject().get("mysteryItems").getAsJsonArray().size();
                }
            }
        }

        if (obj.has("pushDevices")) {
            user.setPushDevices(new ArrayList<>());
            for (JsonElement entry : obj.getAsJsonArray("pushDevices")) {
                PushDevice pushDevice = context.deserialize(entry, PushDevice.class);
                user.getPushDevices().add(pushDevice);
            }
        }

        if (obj.has("lastCron")) {
            user.setLastCron(context.deserialize(obj.get("lastCron"), Date.class));
        }

        if (obj.has("needsCron")) {
            user.setNeedsCron(obj.get("needsCron").getAsBoolean());
        }

        if (obj.has("achievements")) {
            if (obj.getAsJsonObject("achievements").has("streak")) {
                try {
                    user.setStreakCount(obj.getAsJsonObject("achievements").get("streak").getAsInt());
                } catch (UnsupportedOperationException ignored) {}
            }
        }

        return user;
    }
}