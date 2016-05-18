package com.magicmicky.habitrpgwrapper.lib.models.responses;

import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import java.util.List;

public class SkillResponse {

    public HabitRPGUser user;
    public List<HabitRPGUser> partyMembers;
    public Task task;

}
