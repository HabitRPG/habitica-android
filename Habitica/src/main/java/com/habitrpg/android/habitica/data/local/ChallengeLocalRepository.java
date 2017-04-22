package com.habitrpg.android.habitica.data.local;

import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import java.util.List;

import rx.Observable;

public interface ChallengeLocalRepository extends BaseLocalRepository {
    Observable<Challenge> getChallenge(String id);
    Observable<List<Task>> getTasks(Challenge challenge);

    void setUsersGroups(List<Group> group);

    Observable<List<Group>> getGroups();
}
