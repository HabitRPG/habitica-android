package com.habitrpg.android.habitica.data;

import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.PostChallenge;

import rx.Observable;

public interface ChallengeRepository extends BaseRepository  {
    Observable<Challenge> createChallenge(PostChallenge challenge);
    Observable<Challenge> updateChallenge(PostChallenge challenge);
    Observable<Void> deleteChallenge(String challengeId);
}
