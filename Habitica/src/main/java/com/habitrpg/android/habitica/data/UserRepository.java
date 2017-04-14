package com.habitrpg.android.habitica.data;

import android.support.annotation.Nullable;

import com.habitrpg.android.habitica.models.user.HabitRPGUser;

import java.util.Map;

import rx.Observable;

public interface UserRepository extends BaseRepository {

    Observable<HabitRPGUser> getUser(String userID);
    Observable<HabitRPGUser> updateUser(HabitRPGUser user, Map<String, Object> updateData);

    Observable<HabitRPGUser> retrieveUser(Boolean withTasks);

    Observable<HabitRPGUser> revive(HabitRPGUser user);

    void resetTutorial(@Nullable HabitRPGUser user);
}
