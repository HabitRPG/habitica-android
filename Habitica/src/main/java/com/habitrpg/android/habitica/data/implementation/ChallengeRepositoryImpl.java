package com.habitrpg.android.habitica.data.implementation;

import com.github.underscore.$;
import com.habitrpg.android.habitica.data.ChallengeRepository;
import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository;
import com.magicmicky.habitrpgwrapper.lib.api.ApiClient;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.PostChallenge;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TasksOrder;

import java.util.List;
import java.util.Map;

import rx.Observable;


public class ChallengeRepositoryImpl extends BaseRepositoryImpl<ChallengeLocalRepository> implements ChallengeRepository {

    public ChallengeRepositoryImpl(ChallengeLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<Challenge> createChallenge(PostChallenge challenge, List<Task> taskList) {

        Map<String, List<Task>> stringListMap = $.groupBy(taskList, t -> t.getType());

        TasksOrder tasksOrder = new TasksOrder();

        for (Map.Entry<String, List<Task>> entry : stringListMap.entrySet()){
            List<String> taskIdList = $.map(entry.getValue(), t -> t.getId());

            switch(entry.getKey()) {
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

        challenge.tasksOrder = tasksOrder;

        return Observable.create(subscriber -> {
            apiClient.createChallenge(challenge).subscribe(challenge1 -> {
                apiClient.createChallengeTasks(challenge1.id, taskList).subscribe(tasks -> {
                    subscriber.onNext(challenge1);
                    subscriber.onCompleted();
                }, throwable ->
                        subscriber.onError(throwable));
            }, throwable ->
                    subscriber.onError(throwable));
        });
    }

    @Override
    public Observable<Challenge> updateChallenge(PostChallenge challenge, List<Task> taskList) {
        return apiClient.updateChallenge(challenge);
    }

    @Override
    public Observable<Void> deleteChallenge(String challengeId) {
        return apiClient.deleteChallenge(challengeId);
    }
}
