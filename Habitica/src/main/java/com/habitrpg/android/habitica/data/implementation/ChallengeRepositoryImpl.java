package com.habitrpg.android.habitica.data.implementation;

import com.github.underscore.U;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.ChallengeRepository;
import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository;
import com.habitrpg.android.habitica.models.LeaveChallengeBody;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.tasks.TasksOrder;
import com.habitrpg.android.habitica.models.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import rx.Observable;
import rx.schedulers.Schedulers;


public class ChallengeRepositoryImpl extends BaseRepositoryImpl<ChallengeLocalRepository> implements ChallengeRepository {

    public ChallengeRepositoryImpl(ChallengeLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<Challenge> getChallenge(String challengeId) {
        return apiClient.getChallenge(challengeId);
    }

    @Override
    public Observable<TaskList> getChallengeTasks(String challengeId) {
        return apiClient.getChallengeTasks(challengeId);
    }


    private TasksOrder getTaskOrders(List<Task> taskList) {
        Map<String, List<Task>> stringListMap = U.groupBy(taskList, t -> t.getType());

        TasksOrder tasksOrder = new TasksOrder();

        for (Map.Entry<String, List<Task>> entry : stringListMap.entrySet()) {
            List<String> taskIdList = U.map(entry.getValue(), t -> t.getId());

            switch (entry.getKey()) {
                case Task.TYPE_HABIT:
                    tasksOrder.setHabits(taskIdList);
                    break;
                case Task.TYPE_DAILY:
                    tasksOrder.setDailys(taskIdList);
                    break;
                case Task.TYPE_TODO:
                    tasksOrder.setTodos(taskIdList);
                    break;
                case Task.TYPE_REWARD:
                    tasksOrder.setRewards(taskIdList);
                    break;
            }
        }

        return tasksOrder;
    }

    private Observable<List<Task>> addChallengeTasks(String challengeId, List<Task> addedTaskList) {
        return apiClient.createChallengeTasks(challengeId, addedTaskList);
    }

    @Override
    public Observable<Challenge> createChallenge(Challenge challenge, List<Task> taskList) {
        challenge.tasksOrder = getTaskOrders(taskList);

        return Observable.create(subscriber -> {
            apiClient.createChallenge(challenge).subscribe(challenge1 -> {
                addChallengeTasks(challenge1.id, taskList).subscribe(task -> {
                    subscriber.onNext(challenge1);
                    subscriber.onCompleted();
                }, subscriber::onError);

            }, subscriber::onError);
        });
    }

    @Override
    public Observable<Challenge> updateChallenge(Challenge challenge, List<Task> fullTaskList,
                                                 List<Task> addedTaskList, List<Task> updatedTaskList, List<String> removedTaskList) {

        ArrayList<Observable> observablesToWait = new ArrayList<>(U.map(updatedTaskList, t -> apiClient.updateTask(t.getId(), t)));
        observablesToWait.addAll(U.map(removedTaskList, apiClient::deleteTask));

        if (addedTaskList.size() != 0) {
            observablesToWait.add(addChallengeTasks(challenge.id, addedTaskList));
        }

        challenge.tasksOrder = getTaskOrders(fullTaskList);

        return Observable.from(observablesToWait)
                .flatMap(task -> task.subscribeOn(Schedulers.computation()))
                .toList()
                .flatMap(tasks -> apiClient.updateChallenge(challenge));
    }

    @Override
    public Observable<Void> deleteChallenge(String challengeId) {
        return apiClient.deleteChallenge(challengeId);
    }

    @Override
    public Observable<RealmResults<Challenge>> getChallenges() {
        return localRepository.getChallenges();
    }

    @Override
    public Observable<RealmResults<Challenge>> getUserChallenges(String userId) {
        return localRepository.getUserChallenges(userId);
    }

    @Override
    public Observable<List<Challenge>> retrieveChallenges(User user) {
        return apiClient.getUserChallenges()
                .doOnNext(localRepository::saveChallenges);
    }

    @Override
    public Observable<Void> leaveChallenge(Challenge challenge, LeaveChallengeBody leaveChallengeBody) {
        if (challenge == null) {
            return Observable.just(null);
        }
        return apiClient.leaveChallenge(challenge.id, leaveChallengeBody)
                .doOnNext(aVoid -> localRepository.setParticipating(challenge, false));
    }

    @Override
    public Observable<Challenge> joinChallenge(Challenge challenge) {
        if (challenge == null) {
            return Observable.just(null);
        }
        return apiClient.joinChallenge(challenge.id)
                .doOnNext(aVoid -> localRepository.setParticipating(challenge, true));
    }
}
