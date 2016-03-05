package com.habitrpg.android.habitica.callbacks;

import com.crashlytics.android.Crashlytics;
import com.habitrpg.android.habitica.events.SkillUsedEvent;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;

import org.greenrobot.eventbus.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by viirus on 28/11/15.
 */
public class SkillCallback implements Callback<HabitRPGUser> {

    private Skill usedSkill;
    private final HabitRPGUserCallback.OnUserReceived callback;

    public SkillCallback(HabitRPGUserCallback.OnUserReceived callback, Skill usedSkill) {
        this.callback = callback;
        this.usedSkill = usedSkill;
    }

    @Override
    public void success(HabitRPGUser habitRPGUser, Response response) {
        habitRPGUser.async().save();
        EventBus.getDefault().post(new SkillUsedEvent(this.usedSkill, habitRPGUser.getStats().getMp()));
        callback.onUserReceived(habitRPGUser);
    }

    @Override
    public void failure(RetrofitError error) {
        callback.onUserFail();
    }
}
