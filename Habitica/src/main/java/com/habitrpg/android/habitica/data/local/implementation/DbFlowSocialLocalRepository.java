package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.SocialLocalRepository;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

import rx.Observable;


public class DbFlowSocialLocalRepository implements SocialLocalRepository {
    @Override
    public void close() {

    }

    @Override
    public Observable<Challenge> getChallenge(String challengeId) {
        return Observable.defer(() -> Observable.just(new Select().from(Challenge.class).where(Condition.column("id").eq(challengeId)).querySingle()));
    }

    @Override
    public Observable<List<Challenge>> getChallenges() {
        return Observable.defer(() -> Observable.just(new Select().from(Challenge.class).where(Condition.column("name").isNotNull()).queryList()));
    }

    @Override
    public Observable<List<Challenge>> getUserChallenges(String userId) {
        return Observable.defer(() -> Observable.just(new Select()
                .from(Challenge.class)
                .where(Condition.column("name").isNotNull())
                .and(Condition.column("user_id").is(userId))
                .queryList()));
    }
}
