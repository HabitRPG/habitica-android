package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;

public class RealmChallengeLocalRepository extends RealmBaseLocalRepository implements ChallengeLocalRepository {

    public RealmChallengeLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public Observable<Challenge> getChallenge(String id) {
        return realm.where(Challenge.class)
                .equalTo("id", id)
                .findFirstAsync()
                .asObservable()
                .filter(realmObject -> realmObject.isLoaded())
                .cast(Challenge.class);
    }

    @Override
    public Observable<List<Task>> getTasks(Challenge challenge) {
        return null;
    }

    @Override
    public Observable<RealmResults<Challenge>> getChallenges() {
        return realm.where(Challenge.class)
                .isNotNull("name")
                .findAllSortedAsync("memberCount", Sort.DESCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Challenge>> getUserChallenges(String userId) {
        return realm.where(Challenge.class)
                .isNotNull("name")
                .equalTo("isParticipating", true)
                .findAllSortedAsync("memberCount", Sort.DESCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public void saveChallenges(User user, List<Challenge> challenges) {
        realm.executeTransactionAsync(realm1 -> realm1.insertOrUpdate(challenges));
    }

    @Override
    public void setParticipating(Challenge challenge, boolean isParticipating) {
        realm.executeTransaction(realm1 -> challenge.isParticipating = isParticipating);
    }
}
