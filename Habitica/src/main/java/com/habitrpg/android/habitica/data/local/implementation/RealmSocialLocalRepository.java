package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.SocialLocalRepository;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.ChatMessage;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;


public class RealmSocialLocalRepository extends RealmBaseLocalRepository implements SocialLocalRepository {

    public RealmSocialLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public Observable<Challenge> getChallenge(String challengeId) {
        return realm.where(Challenge.class)
                .equalTo("id", challengeId)
                .findFirstAsync()
                .asObservable()
                .filter(realmObject -> realmObject.isLoaded())
                .cast(Challenge.class);
    }

    @Override
    public Observable<RealmResults<Challenge>> getChallenges() {
        return realm.where(Challenge.class)
                .isNotNull("name")
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Challenge>> getUserChallenges(String userId) {
        return realm.where(Challenge.class)
                .isNotNull("name")
                .equalTo("userId", userId)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }
}
