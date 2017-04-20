package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;
import com.habitrpg.android.habitica.models.TutorialStep;

import java.util.List;

import rx.Observable;

public interface UserLocalRepository extends BaseLocalRepository {

    Observable<HabitRPGUser> getUser(String userID);

    void saveUser(HabitRPGUser user);

    Observable<List<TutorialStep>> getTutorialSteps();

    Observable<List<Skill>> getSkills(HabitRPGUser user);

    Observable<List<Skill>> getSpecialItems(HabitRPGUser user);
}
