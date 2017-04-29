package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.ChatMessage;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;

public interface SocialLocalRepository extends BaseLocalRepository {
    Observable<Challenge> getChallenge(String challengeId);

    Observable<RealmResults<Challenge>> getChallenges();

    Observable<RealmResults<Challenge>> getUserChallenges(String userId);
}
