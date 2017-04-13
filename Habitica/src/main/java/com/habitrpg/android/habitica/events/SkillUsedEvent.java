package com.habitrpg.android.habitica.events;

import com.habitrpg.android.habitica.models.Skill;

/**
 * Created by viirus on 28/11/15.
 */
public class SkillUsedEvent {

    public Skill usedSkill;
    public Double newMana, xp, hp, gold;

    public SkillUsedEvent(Skill usedSkill, Double newMana, Double xp, Double hp, Double gold) {
        this.usedSkill = usedSkill;
        this.newMana = newMana;
        this.xp = xp;
        this.hp = hp;
        this.gold = gold;
    }
}
