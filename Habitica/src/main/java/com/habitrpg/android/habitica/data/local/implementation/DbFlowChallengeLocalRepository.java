package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

import rx.Observable;

public class DbFlowChallengeLocalRepository implements ChallengeLocalRepository {

    @Override
    public Observable<Challenge> getChallenge(String id) {
        return Observable.defer(() -> Observable.just(new Select().from(Challenge.class).where(Condition.column("id").is(id)).querySingle()));
    }

    @Override
    public Observable<List<Task>> getTasks(Challenge challenge) {
        return null;
    }

    @Override
    public void setUsersGroups(List<Group> groups) {

        new Delete().from(Group.class).query();


        for (Group group : groups) {
            group.isMember = true;
            group.async().save();
        }
    }

    @Override
    public Observable<List<Group>> getGroups() {
        return Observable.defer(() -> Observable.just(new Select().from(Group.class).where(Condition.column("isMember").is(true)).queryList()));
    }


    @Override
    public void close() {

    }
}
