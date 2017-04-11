package com.habitrpg.android.habitica.data.local;

import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import rx.Observable;

public interface UserLocalRepository extends BaseLocalRepository {

    Observable<HabitRPGUser> getUser(String userID);

    void saveUser(HabitRPGUser user);
}
