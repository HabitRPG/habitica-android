package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
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
        return Observable.create(subscriber -> {
            new Select().from(Challenge.class).where(Condition.column("id").is(id)).async().querySingle(new TransactionListener<Challenge>() {
                @Override
                public void onResultReceived(Challenge result) {
                    subscriber.onNext(result);
                    subscriber.onCompleted();
                }

                @Override
                public boolean onReady(BaseTransaction<Challenge> transaction) {
                    return false;
                }

                @Override
                public boolean hasResult(BaseTransaction<Challenge> transaction, Challenge result) {
                    return true;
                }
            });
        });
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
        return Observable.create(subscriber -> {
            new Select().from(Group.class).where(Condition.column("isMember").is(true)).async().queryList(new TransactionListener<List<Group>>() {
                @Override
                public void onResultReceived(List<Group> result) {
                    subscriber.onNext(result);
                    subscriber.onCompleted();
                }

                @Override
                public boolean onReady(BaseTransaction<List<Group>> transaction) {
                    return false;
                }

                @Override
                public boolean hasResult(BaseTransaction<List<Group>> transaction, List<Group> result) {
                    return true;
                }
            });
        });
    }


    @Override
    public void close() {

    }
}
