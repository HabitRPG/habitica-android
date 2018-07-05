package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.LeaveChallengeBody;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

import io.reactivex.Flowable;
import io.realm.RealmResults;

public interface ChallengeRepository extends BaseRepository  {
    Flowable<Challenge> getChallenge(String challengeId);
    Flowable<TaskList> getChallengeTasks(String challengeId);

    Flowable<Challenge> createChallenge(Challenge challenge, List<Task> taskList);

    /**
     *
     * @param challenge the challenge that will be updated
     * @param fullTaskList lists all tasks of the current challenge, to create the taskOrders
     * @param addedTaskList only the tasks to be added online
     * @param updatedTaskList only the updated ones
     * @param removedTaskList tasks that has be to be removed
     * @return Observable with the updated challenge
     */
    Flowable<Challenge> updateChallenge(Challenge challenge, List<Task> fullTaskList,
                                          List<Task> addedTaskList, List<Task> updatedTaskList, List<String> removedTaskList);
    Flowable<Void> deleteChallenge(String challengeId);

    Flowable<RealmResults<Challenge>> getChallenges();
    Flowable<RealmResults<Challenge>> getUserChallenges(String userId);

    Flowable<List<Challenge>> retrieveChallenges(User user);

    Flowable<Void> leaveChallenge(Challenge challenge, LeaveChallengeBody leaveChallengeBody);

    Flowable<Challenge> joinChallenge(Challenge challenge);
}
