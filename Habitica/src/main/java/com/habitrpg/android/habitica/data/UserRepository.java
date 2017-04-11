package com.habitrpg.android.habitica.data;

import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import java.util.Map;

import rx.Observable;

public interface UserRepository extends BaseRepository {

    Observable<HabitRPGUser> getUser(String userID);
    Observable<HabitRPGUser> updateUser(HabitRPGUser user, Map<String, Object> updateData);

    Observable<HabitRPGUser> retrieveUser(Boolean withTasks);

    Observable<HabitRPGUser> revive(HabitRPGUser user);
}
