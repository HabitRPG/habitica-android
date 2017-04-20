package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.data.local.UserLocalRepository;
import com.habitrpg.android.habitica.helpers.ReactiveErrorHandler;
import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.TutorialStep;
import com.habitrpg.android.habitica.models.responses.SkillResponse;
import com.habitrpg.android.habitica.models.user.User;

import java.util.HashMap;
import java.util.Map;

import io.realm.RealmResults;
import rx.Observable;


public class UserRepositoryImpl extends BaseRepositoryImpl<UserLocalRepository> implements UserRepository {

    public UserRepositoryImpl(UserLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<User> getUser(String userID) {
        return localRepository.getUser(userID)
                .first()
                .flatMap(habitRPGUser -> {
                    if (habitRPGUser == null || !habitRPGUser.isValid()) {
                        return retrieveUser(true);
                    } else {
                        return Observable.just(habitRPGUser);
                    }
                });
    }

    @Override
    public Observable<User> updateUser(User user, Map<String, Object> updateData) {
        return apiClient.updateUser(updateData)
                .map(newUser -> mergeUser(user, newUser));
    }

    @Override
    public Observable<User> updateUser(User user, String key, Object value) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(key, value);
        return updateUser(user, updateData);
    }

    @Override
    public Observable<User> retrieveUser(Boolean withTasks) {
        return apiClient.retrieveUser(withTasks)
                .doOnNext(localRepository::saveUser);
    }

    @Override
    public Observable<User> revive(User user) {
        return apiClient.revive().map(newUser -> mergeUser(user, newUser));
    }

    @Override
    public void resetTutorial(User user) {
        localRepository.getTutorialSteps()
                .map(tutorialSteps -> {
                    Map<String, Object> updateData = new HashMap<>();
                    for (TutorialStep step : tutorialSteps) {
                        updateData.put("flags.tutorial." + step.getTutorialGroup() + "." + step.getIdentifier(), false);
                    }
                    return updateData;
                })
                .flatMap(updateData -> updateUser(user, updateData))
                .subscribe(tutorialSteps -> {}, throwable -> {});
    }

    @Override
    public Observable<User> sleep(User user) {
        return apiClient.sleep()
                .map(isSleeping -> {
                    user.getPreferences().setSleep(isSleeping);
                    localRepository.saveUser(user);
                    return user;
                });
    }

    @Override
    public Observable<RealmResults<Skill>> getSkills(User user) {
        return localRepository.getSkills(user);
    }

    @Override
    public Observable<RealmResults<Skill>> getSpecialItems(User user) {
        return localRepository.getSpecialItems(user);
    }

    @Override
    public Observable<SkillResponse> useSkill(User user, String key, String target, String taskId) {
        return apiClient.useSkill(key, target, taskId).doOnNext(skillResponse -> {
            if (user != null) {
                mergeUser(user, skillResponse.user);
            }
        });
    }

    @Override
    public Observable<SkillResponse> useSkill(User user, String key, String target) {
        return apiClient.useSkill(key, target).doOnNext(skillResponse -> {
            if (user != null) {
                mergeUser(user, skillResponse.user);
            }
        });
    }

    private User mergeUser(User oldUser, User newUser) {
        if (newUser.getItems() != null) {
            oldUser.setItems(newUser.getItems());
        }
        if (newUser.getPreferences() != null) {
            oldUser.setPreferences(newUser.getPreferences());
        }
        if (newUser.getFlags() != null) {
            oldUser.setFlags(newUser.getFlags());
        }
        if (newUser.getStats() != null) {
            oldUser.getStats().merge(newUser.getStats());
        }

        localRepository.saveUser(oldUser);
        return oldUser;
    }
}
