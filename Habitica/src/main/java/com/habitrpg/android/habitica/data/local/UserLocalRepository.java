package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.HabitRPGUser;
import com.habitrpg.android.habitica.models.TutorialStep;

import java.util.List;

import rx.Observable;

public interface UserLocalRepository extends BaseLocalRepository {

    Observable<HabitRPGUser> getUser(String userID);

    void saveUser(HabitRPGUser user);

    Observable<List<TutorialStep>> getTutorialSteps();
}
