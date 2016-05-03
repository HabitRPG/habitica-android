package com.habitrpg.android.habitica.events;

import com.magicmicky.habitrpgwrapper.lib.models.Skill;

/**
 * Created by viirus on 28/11/15.
 */
public class SkillUsedEvent {

    public Skill usedSkill;
    public Double newMana;

    public SkillUsedEvent(Skill usedSkill, Double newMana) {
        this.usedSkill = usedSkill;
        this.newMana = newMana;
    }
}
