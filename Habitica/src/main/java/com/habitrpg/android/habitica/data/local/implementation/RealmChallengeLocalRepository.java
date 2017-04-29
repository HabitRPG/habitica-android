package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.tasks.Task;

import java.util.List;

import io.realm.Realm;
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
    public void setUsersGroups(List<Group> groups) {

    }

    @Override
    public Observable<List<Group>> getGroups() {
        return null;
    }
}
