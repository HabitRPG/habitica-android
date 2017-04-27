package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.data.local.UserLocalRepository;
import com.habitrpg.android.habitica.helpers.ReactiveErrorHandler;
import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.TutorialStep;
import com.habitrpg.android.habitica.models.responses.SkillResponse;
import com.habitrpg.android.habitica.models.tasks.RemindersItem;
import com.habitrpg.android.habitica.models.user.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.RealmResults;
import rx.Observable;


public class UserRepositoryImpl extends BaseRepositoryImpl<UserLocalRepository> implements UserRepository {

    private Date lastSync;

    public UserRepositoryImpl(UserLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<User> getUser(String userID) {
        return localRepository.getUser(userID);
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
        return retrieveUser(withTasks, false);
    }

    @Override
    public Observable<User> retrieveUser(Boolean withTasks, Boolean forced) {
        if (forced || this.lastSync == null || (new Date().getTime() - this.lastSync.getTime()) > 180000) {
            lastSync = new Date();
            return apiClient.retrieveUser(withTasks)
                    .doOnNext(localRepository::saveUser);
        } else {
            return Observable.empty();
        }
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
                .subscribe(tutorialSteps -> {}, ReactiveErrorHandler.handleEmptyError());
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
        return apiClient.useSkill(key, target)
                .map(response -> {
                    response.hpDiff = response.user.getStats().getHp() - user.getStats().getHp();
                    response.expDiff = response.user.getStats().getExp() - user.getStats().getExp();
                    response.goldDiff = response.user.getStats().getGp() - user.getStats().getGp();
                    return response;
                })
                .doOnNext(skillResponse -> {
            if (user != null) {
                mergeUser(user, skillResponse.user);
            }
        });
    }

    @Override
    public Observable<User> changeClass() {
        return apiClient.changeClass();
    }

    @Override
    public Observable<User> disableClasses() {
        return apiClient.disableClasses();
    }

    @Override
    public Observable<User> changeClass(String selectedClass) {
        return changeClass(selectedClass);
    }

    private User mergeUser(User oldUser, User newUser) {
        User copiedUser = localRepository.getUnmanagedCopy(oldUser);
        if (newUser.getItems() != null) {
            copiedUser.setItems(newUser.getItems());
        }
        if (newUser.getPreferences() != null) {
            copiedUser.setPreferences(newUser.getPreferences());
        }
        if (newUser.getFlags() != null) {
            copiedUser.setFlags(newUser.getFlags());
        }
        if (newUser.getStats() != null) {
            copiedUser.getStats().merge(newUser.getStats());
        }

        localRepository.saveUser(copiedUser);
        return oldUser;
    }
}
