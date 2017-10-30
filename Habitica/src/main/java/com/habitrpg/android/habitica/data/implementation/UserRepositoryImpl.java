package com.habitrpg.android.habitica.data.implementation;

import android.support.annotation.Nullable;

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
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.models.user.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.realm.RealmResults;
import rx.Observable;


public class UserRepositoryImpl extends BaseRepositoryImpl<UserLocalRepository> implements UserRepository {

    private Date lastSync;
    private String userId;

    private TaskRepository taskRepository;

    public UserRepositoryImpl(UserLocalRepository localRepository, ApiClient apiClient, String userId, TaskRepository taskRepository) {
        super(localRepository, apiClient);
        this.taskRepository = taskRepository;
        this.userId = userId;
    }

    @Override
    public Observable<User> getUser(String userID) {
        return localRepository.getUser(userID);
    }

    @Override
    public Observable<User> getUser() {
        return getUser(userId);
    }

    @Override
    public Observable<User> updateUser(@Nullable User user, Map<String, Object> updateData) {
        if (user == null) {
            return Observable.just(null);
        }
        return apiClient.updateUser(updateData)
                .map(newUser -> mergeUser(user, newUser));
    }

    @Override
    public Observable<User> updateUser(@Nullable User user, String key, Object value) {
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
                    .doOnNext(user -> {
                        if (withTasks) {
                            taskRepository.saveTasks(user.getId(), user.getTasksOrder(), user.tasks);
                        }
                    })
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
            return getUser();
        }
    }

    @Override
    public Observable<RealmResults<ChatMessage>> getInboxMessages(String replyToUserID) {
        return localRepository.getInboxMessages(userId, replyToUserID);
    }

    @Override
    public Observable<RealmResults<ChatMessage>> getInboxOverviewList() {
        return localRepository.getInboxOverviewList(userId);
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
        localRepository.executeTransaction(realm -> user.getPreferences().setSleep(!user.getPreferences().getSleep()));
        return apiClient.sleep()
                .map(isSleeping -> user);
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
    public Observable<List> readNotification(String id) {
        return apiClient.readNotification(id);
    }

    @Override
    public Observable<User> changeCustomDayStart(int dayStartTime) {
        Map<String, Object> updateObject = new HashMap<>();
        updateObject.put("dayStart", dayStartTime);
        return apiClient.changeCustomDayStart(updateObject);
    }

    @Override
    public Observable<User> updateLanguage(User user, String languageCode) {
        return updateUser(user, "preferences.language", languageCode)
                .doOnNext(user1 -> apiClient.setLanguageCode(languageCode));
    }

    @Override
    public Observable<User> resetAccount() {
        return apiClient.resetAccount()
                .flatMap(aVoid -> retrieveUser(true, true));
    }

    @Override
    public Observable<Void> deleteAccount(String password) {
        return apiClient.deleteAccount(password);
    }

    @Override
    public Observable<Void> sendPasswordResetEmail(String email) {
        return apiClient.sendPasswordResetEmail(email);
    }

    @Override
    public Observable<Void> updateLoginName(@NotNull String newLoginName, @NotNull String password) {
        return apiClient.updateLoginName(newLoginName, password);
    }

    @Override
    public Observable<Void> updateEmail(@NotNull String newEmail, @NotNull String password) {
        return apiClient.updateEmail(newEmail, password);
    }

    @Override
    public Observable<Void> updatePassword(@NotNull String newPassword, @NotNull String oldPassword, String oldPasswordConfirmation) {
        return apiClient.updatePassword(newPassword, oldPassword, oldPasswordConfirmation);
    }

    @NotNull
    @Override
    public Observable<Stats> allocatePoint(@Nullable User user, String stat) {
        if (user != null && user.isManaged()) {
            localRepository.executeTransaction(realm -> {
                if (Stats.STRENGTH.equals(stat)) {
                    user.getStats().str += 1;
                } else if (Stats.INTELLIGENCE.equals(stat)) {
                    user.getStats()._int += 1;
                } else if (Stats.CONSTITUTION.equals(stat)) {
                    user.getStats().con += 1;
                } else if (Stats.PERCEPTION.equals(stat)) {
                    user.getStats().per += 1;
                }
                user.getStats().points -= 1;
            });
        }
        return apiClient.allocatePoint(stat)
                .doOnNext(stats -> {
                    if (user != null && user.isManaged()) {
                        localRepository.executeTransaction(realm -> {
                            user.getStats().str = stats.str;
                            user.getStats().con = stats.con;
                            user.getStats().per = stats.per;
                            user.getStats()._int = stats._int;
                            user.getStats().points = stats.points;
                            user.getStats().mp = stats.mp;
                        });
                    }
                });
    }

    @NotNull
    @Override
    public Observable<Stats> bulkAllocatePoints(int strength, int intelligence, int constitution, int perception) {
        return apiClient.bulkAllocatePoints(strength, intelligence, constitution, perception);
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
        localRepository.getUser(userId).first().subscribe(user -> localRepository.executeTransaction(realm -> user.setNeedsCron(false)), RxErrorHandler.handleEmptyError());
        observable.flatMap(taskScoringResults -> apiClient.runCron())
                .flatMap(aVoid -> this.retrieveUser(true, true))
                .subscribe(user -> {}, RxErrorHandler.handleEmptyError());
    }

    private User mergeUser(User oldUser, User newUser) {
        if (oldUser == null || !oldUser.isValid()) {
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
