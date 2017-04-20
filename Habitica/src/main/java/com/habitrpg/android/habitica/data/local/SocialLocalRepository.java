package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.social.Challenge;

import java.util.List;

import rx.Observable;

public interface SocialLocalRepository extends BaseLocalRepository {
    Observable<Challenge> getChallenge(String challengeId);

    Observable<List<Challenge>> getChallenges();

    Observable<List<Challenge>> getUserChallenges(String userId);
}
