package com.habitrpg.android.habitica.data.local;


import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;

public interface ChallengeLocalRepository extends BaseLocalRepository {
    Observable<Challenge> getChallenge(String id);
    Observable<List<Task>> getTasks(Challenge challenge);

    Observable<RealmResults<Challenge>> getChallenges();

    Observable<RealmResults<Challenge>> getUserChallenges(String userId);

    void setParticipating(Challenge challenge, boolean isParticipating);
}
