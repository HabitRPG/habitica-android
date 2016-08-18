package com.habitrpg.android.habitica.callbacks;

import com.habitrpg.android.habitica.events.SkillUsedEvent;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.magicmicky.habitrpgwrapper.lib.models.responses.SkillResponse;

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
            usedSkill.xp = user.getStats().getExp() - this.user.getStats().getExp();
            usedSkill.hp = user.getStats().getHp() - this.user.getStats().getHp();
            usedSkill.gold = user.getStats().getGp() - this.user.getStats().getGp();
            this.user.getStats().merge(user.getStats());
        }

        this.user.async().save();

        callBack.onUserReceived(this.user);

        EventBus.getDefault().post(new SkillUsedEvent(this.usedSkill, skillResponse.user.getStats().getMp()));
    }
}
