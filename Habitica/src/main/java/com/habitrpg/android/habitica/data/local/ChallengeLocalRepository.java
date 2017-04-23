package com.habitrpg.android.habitica.data.local;


import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.tasks.Task;

import java.util.List;

import rx.Observable;

public interface ChallengeLocalRepository extends BaseLocalRepository {
    Observable<Challenge> getChallenge(String id);
    Observable<List<Task>> getTasks(Challenge challenge);

    void setUsersGroups(List<Group> group);

    Observable<List<Group>> getGroups();
}
