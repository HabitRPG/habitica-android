package com.habitrpg.android.habitica.data;

import android.support.annotation.Nullable;

import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.inventory.Customization;
import com.habitrpg.android.habitica.models.inventory.CustomizationSet;
import com.habitrpg.android.habitica.models.responses.SkillResponse;
import com.habitrpg.android.habitica.models.responses.UnlockResponse;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import rx.Observable;

public interface UserRepository extends BaseRepository {

    Observable<User> getUser(String userID);
    Observable<User> updateUser(User user, Map<String, Object> updateData);
    Observable<User> updateUser(User user, String key, Object value);

    Observable<User> retrieveUser(Boolean withTasks);
    Observable<User> retrieveUser(Boolean withTasks, Boolean forced);

    Observable<User> revive(User user);

    void resetTutorial(@Nullable User user);

    Observable<User> sleep(User user);

    Observable<RealmResults<Skill>> getSkills(User user);

    Observable<RealmResults<Skill>> getSpecialItems(User user);

    Observable<SkillResponse> useSkill(@Nullable User user, String key, String target, String taskId);

    Observable<SkillResponse> useSkill(@Nullable User user, String key, String target);

    Observable<User> changeClass();

    Observable<User> disableClasses();

    Observable<User> changeClass(String selectedClass);

    Observable<UnlockResponse> unlockPath(User user, Customization customization);
    Observable<UnlockResponse> unlockPath(User user, CustomizationSet set);

    void runCron(List<Task> tasks);
    void runCron();
}
