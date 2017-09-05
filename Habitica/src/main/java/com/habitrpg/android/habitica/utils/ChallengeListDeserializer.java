package com.habitrpg.android.habitica.utils;

import android.support.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.social.Challenge;

import java.lang.reflect.Type;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

public class ChallengeListDeserializer implements JsonDeserializer<List<Challenge>> {
    @Override
    public List<Challenge> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RealmList<Challenge> challenges = new RealmList<>();

        Realm realm = Realm.getDefaultInstance();
        List<Challenge> existingChallenges = realm.copyFromRealm(realm.where(Challenge.class).findAll());
        realm.close();

         for (JsonElement element : json.getAsJsonArray()) {
            Challenge challenge;
            if (element.isJsonObject()) {
                challenge = context.deserialize(element, Challenge.class);
                Challenge existingChallenge = findExistingChallenge(existingChallenges, challenge.id);
                if (existingChallenge != null) {
                    challenge.isParticipating = existingChallenge.isParticipating;
                }
            } else {
                challenge = findExistingChallenge(existingChallenges, element.getAsString());
                if (challenge == null) {
                    challenge = new Challenge();
                    challenge.id = element.getAsString();
                }
                challenge.isParticipating = true;
            }
            challenges.add(challenge);
        }

        return challenges;
    }

    @Nullable
    private Challenge findExistingChallenge(List<Challenge> existingChallenges, String id) {
        for (Challenge challenge : existingChallenges) {
            if (id.equals(challenge.id)) {
                return challenge;
            }
        }
        return null;
    }
}
