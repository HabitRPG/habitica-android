package com.habitrpg.android.habitica.callbacks;

import com.habitrpg.android.habitica.events.SkillUsedEvent;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by viirus on 28/11/15.
 */
public class SkillCallback extends HabitRPGUserCallback  {

    private Skill usedSkill;

    public SkillCallback(HabitRPGUserCallback.OnUserReceived callback, Skill usedSkill) {
        super(callback);
        this.usedSkill = usedSkill;
    }

    @Override
    public void call(HabitRPGUser habitRPGUser) {
        habitRPGUser.async().save();
        EventBus.getDefault().post(new SkillUsedEvent(this.usedSkill, habitRPGUser.getStats().getMp()));
        callBack.onUserReceived(habitRPGUser);
    }
}
