package com.habitrpg.android.habitica.callbacks;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.habitrpg.android.habitica.events.SkillUsedEvent;
import com.habitrpg.android.habitica.events.TaskCreatedEvent;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by viirus on 28/11/15.
 */
public class SkillCallback implements Callback<HabitRPGUser> {

    private final HabitRPGUserCallback.OnUserReceived callback;
    private Skill usedSkill;

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
        Crashlytics.logException(error);
        callback.onUserFail();
    }
}
