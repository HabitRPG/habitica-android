package com.habitrpg.android.habitica.data;

import android.support.annotation.Nullable;

import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.responses.SkillResponse;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;

import java.util.List;
import java.util.Map;

import rx.Observable;

public interface UserRepository extends BaseRepository {

    Observable<HabitRPGUser> getUser(String userID);
    Observable<HabitRPGUser> updateUser(HabitRPGUser user, Map<String, Object> updateData);
    Observable<HabitRPGUser> updateUser(HabitRPGUser user, String key, Object value);

    Observable<HabitRPGUser> retrieveUser(Boolean withTasks);

    Observable<HabitRPGUser> revive(HabitRPGUser user);

    void resetTutorial(@Nullable HabitRPGUser user);

    Observable<HabitRPGUser> sleep(HabitRPGUser user);

    Observable<List<Skill>> getSkills(HabitRPGUser user);

    Observable<List<Skill>> getSpecialItems(HabitRPGUser user);

    Observable<SkillResponse> useSkill(@Nullable HabitRPGUser user, String key, String target, String taskId);

    Observable<SkillResponse> useSkill(@Nullable HabitRPGUser user, String key, String target);
}
