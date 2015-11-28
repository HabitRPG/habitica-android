package com.magicmicky.habitrpgwrapper.lib.models;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;

import java.util.HashMap;

/**
 * Created by Negue on 15.07.2015.
 */
public class ContentResult {

    public ItemData potion;

    public ItemData armoire;

    public ContentGear gear;

    public HashMap<String, QuestContent> quests;

    public SkillList spells;
}

