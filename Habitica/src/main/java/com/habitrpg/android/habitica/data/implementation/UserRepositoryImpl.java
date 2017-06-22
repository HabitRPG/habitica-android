package com.habitrpg.android.habitica.data.implementation;

import android.content.Context;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.data.local.UserLocalRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.TutorialStep;
import com.habitrpg.android.habitica.models.inventory.Customization;
import com.habitrpg.android.habitica.models.inventory.CustomizationSet;
import com.habitrpg.android.habitica.models.responses.SkillResponse;
import com.habitrpg.android.habitica.models.responses.TaskScoringResult;
import com.habitrpg.android.habitica.models.responses.UnlockResponse;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.views.yesterdailies.YesterdailyDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;
import rx.functions.Func1;


public class UserRepositoryImpl extends BaseRepositoryImpl<UserLocalRepository> implements UserRepository {

    private final Context context;
    private Date lastSync;
    private String userId;

    private TaskRepository taskRepository;

    public UserRepositoryImpl(UserLocalRepository localRepository, ApiClient apiClient, Context context, String userId, TaskRepository taskRepository) {
        super(localRepository, apiClient);
        this.taskRepository = taskRepository;
        this.context = context;
        this.userId = userId;
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
                    .doOnNext(localRepository::saveUser)
                    .doOnNext(user -> taskRepository.saveTasks(user.getId(), user.getTasksOrder(), user.tasks))
                    .flatMap(user -> {
                        Calendar calendar = new GregorianCalendar();
                        TimeZone timeZone = calendar.getTimeZone();
                        long offset = -TimeUnit.MINUTES.convert(timeZone.getOffset(calendar.getTimeInMillis()), TimeUnit.MILLISECONDS);
                        if (user != null && user.getPreferences() != null && offset != user.getPreferences().getTimezoneOffset()) {
                            return updateUser(user, "preferences.timezoneOffset", String.valueOf(offset));
                        } else {
                            return Observable.just(user);
                        }
                    });
        } else {
            return Observable.just(null);
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
                .subscribe(tutorialSteps -> {}, RxErrorHandler.handleEmptyError());
    }

    @Override
    public Observable<User> sleep(User user) {
        return apiClient.sleep()
                .map(isSleeping -> {
                    localRepository.executeTransaction(realm -> user.getPreferences().setSleep(isSleeping));
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
        return apiClient.changeClass(selectedClass);
    }

    @Override
    public Observable<UnlockResponse> unlockPath(User user, Customization customization) {
        return apiClient.unlockPath(customization.getPath())
                .doOnNext(unlockResponse -> {
                    User copiedUser = localRepository.getUnmanagedCopy(user);
                    copiedUser.setPreferences(unlockResponse.preferences);
                    copiedUser.setPurchased(unlockResponse.purchased);
                    copiedUser.setItems(unlockResponse.items);
                    copiedUser.setBalance(copiedUser.getBalance()-customization.getPrice()/4.0);
                    localRepository.saveUser(copiedUser);
                });
    }

    @Override
    public Observable<UnlockResponse> unlockPath(User user, CustomizationSet set) {
        String path = "";
        for (Customization customization : set.customizations) {
            path = path + "," + customization.getPath();
        }
        if (path.length() == 0) {
            return Observable.just(null);
        }
        path = path.substring(1);
        return apiClient.unlockPath(path)
                .doOnNext(unlockResponse -> {
                    User copiedUser = localRepository.getUnmanagedCopy(user);
                    copiedUser.setPreferences(unlockResponse.preferences);
                    copiedUser.setPurchased(unlockResponse.purchased);
                    copiedUser.setItems(unlockResponse.items);
                    copiedUser.setBalance(copiedUser.getBalance()-set.price/4.0);
                    localRepository.saveUser(copiedUser);
                });
    }

    @Override
    public void runCron() {
        runCron(new ArrayList<>());
    }

    @Override
    public void runCron(List<Task> tasks) {
        Observable<List<TaskScoringResult>> observable;
        if (tasks.size() > 0) {
            observable = Observable.from(tasks)
                    .flatMap(task -> taskRepository.taskChecked(null, task, true, true))
                    .toList();
        } else {
            observable = Observable.just(null);
        }
        observable.flatMap(taskScoringResults -> apiClient.runCron())
                .flatMap(aVoid -> this.retrieveUser(true))
                .subscribe(user -> {}, RxErrorHandler.handleEmptyError());
    }

    private User mergeUser(User oldUser, User newUser) {
        if (!oldUser.isValid()) {
            return oldUser;
        }
        User copiedUser;
        if (oldUser.isManaged()) {
            copiedUser = localRepository.getUnmanagedCopy(oldUser);
        } else {
            copiedUser = oldUser;
        }
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
