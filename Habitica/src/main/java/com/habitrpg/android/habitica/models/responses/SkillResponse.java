package com.habitrpg.android.habitica.models.responses;

import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.shared.habitica.models.user.User;

import java.util.List;

public class SkillResponse {

    public User user;
    public List<User> partyMembers;
    public Task task;

    public double expDiff;
    public double hpDiff;
    public double goldDiff;

}
