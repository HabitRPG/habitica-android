package com.habitrpg.android.habitica.callbacks;

import com.habitrpg.android.habitica.events.SkillUsedEvent;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;
import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.models.responses.SkillResponse;

import org.greenrobot.eventbus.EventBus;

import rx.functions.Action1;

/**
 * Created by viirus on 28/11/15.
 */
public class SkillCallback implements Action1<SkillResponse> {

    private Skill usedSkill;
    private HabitRPGUser user;
    private HabitRPGUserCallback.OnUserReceived callBack;

    public SkillCallback(HabitRPGUserCallback.OnUserReceived callback, HabitRPGUser user, Skill usedSkill) {
        this.usedSkill = usedSkill;
        this.user = user;
        this.callBack = callback;
    }

    @Override
    public void call(SkillResponse skillResponse) {
        Double xp = this.user.getStats().getExp();
        Double hp = this.user.getStats().getHp();
        Double gold = this.user.getStats().getGp();

        HabitRPGUser user = skillResponse.user;
        if (user.getItems() != null) {
            this.user.setItems(user.getItems());
        }
        if (user.getPreferences() != null) {
            this.user.setPreferences(user.getPreferences());
        }
        if (user.getFlags() != null) {
            this.user.setFlags(user.getFlags());
        }
        if (user.getStats() != null) {
            this.user.getStats().merge(user.getStats());
        }

        this.user.async().save();

        callBack.onUserReceived(this.user);
        Stats stats = skillResponse.user.getStats();
        EventBus.getDefault().post(new SkillUsedEvent(this.usedSkill, stats.getMp(), stats.getExp() - xp, stats.getHp() - hp, stats.getGp() - gold));
    }
}
