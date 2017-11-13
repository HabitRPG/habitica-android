package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.LeaveChallengeBody;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;

public interface ChallengeRepository extends BaseRepository  {
    Observable<Challenge> getChallenge(String challengeId);
    Observable<TaskList> getChallengeTasks(String challengeId);

    Observable<Challenge> createChallenge(Challenge challenge, List<Task> taskList);

    /**
     *
     * @param challenge the challenge that will be updated
     * @param fullTaskList lists all tasks of the current challenge, to create the taskOrders
     * @param addedTaskList only the tasks to be added online
     * @param updatedTaskList only the updated ones
     * @param removedTaskList tasks that has be to be removed
     * @return Observable with the updated challenge
     */
    Observable<Challenge> updateChallenge(Challenge challenge, List<Task> fullTaskList,
                                          List<Task> addedTaskList, List<Task> updatedTaskList, List<String> removedTaskList);
    Observable<Void> deleteChallenge(String challengeId);

    Observable<RealmResults<Challenge>> getChallenges();
    Observable<RealmResults<Challenge>> getUserChallenges(String userId);

    Observable<List<Challenge>> retrieveChallenges(User user);

    Observable<Void> leaveChallenge(Challenge challenge, LeaveChallengeBody leaveChallengeBody);

    Observable<Challenge> joinChallenge(Challenge challenge);
}
