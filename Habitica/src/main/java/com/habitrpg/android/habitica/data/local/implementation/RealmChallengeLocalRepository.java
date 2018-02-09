package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import io.realm.OrderedRealmCollectionSnapshot;
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
        return getRealm().where(Challenge.class)
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
        return getRealm().where(Challenge.class)
                .isNotNull("name")
                .findAllSortedAsync("official", Sort.DESCENDING, "createdAt", Sort.DESCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Challenge>> getUserChallenges(String userId) {
        return getRealm().where(Challenge.class)
                .isNotNull("name")
                .equalTo("isParticipating", true)
                .findAllSortedAsync("official", Sort.DESCENDING, "createdAt", Sort.DESCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public void setParticipating(Challenge challenge, boolean isParticipating) {
        getRealm().executeTransaction(realm1 -> challenge.isParticipating = isParticipating);
    }

    @Override
    public void saveChallenges(List<Challenge> onlineChallenges) {
        OrderedRealmCollectionSnapshot<Challenge> localChallenges = getRealm().where(Challenge.class).findAll().createSnapshot();
        List<Challenge> challengesToDelete = new ArrayList<>();
        for (Challenge localTask : localChallenges) {
            if (!onlineChallenges.contains(localTask)) {
                challengesToDelete.add(localTask);
            }
        }
        getRealm().executeTransaction(realm1 -> {
            for (Challenge localTask : challengesToDelete) {
                localTask.deleteFromRealm();
            }
        });
        getRealm().executeTransaction(realm1 -> realm1.insertOrUpdate(onlineChallenges));
    }
}
