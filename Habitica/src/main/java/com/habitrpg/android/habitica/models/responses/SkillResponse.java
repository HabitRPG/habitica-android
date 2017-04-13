package com.habitrpg.android.habitica.models.responses;

import com.habitrpg.android.habitica.models.HabitRPGUser;
import com.habitrpg.android.habitica.models.tasks.Task;

import java.util.List;

public class SkillResponse {

    public HabitRPGUser user;
    public List<HabitRPGUser> partyMembers;
    public Task task;

}
