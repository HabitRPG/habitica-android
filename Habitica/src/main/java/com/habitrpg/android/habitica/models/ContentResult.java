package com.habitrpg.android.habitica.models;

import com.habitrpg.android.habitica.models.inventory.Customization;
import com.habitrpg.android.habitica.models.inventory.Egg;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.HatchingPotion;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.inventory.SpecialItem;

import java.util.List;

import io.realm.RealmList;

/**
 * Created by Negue on 15.07.2015.
 */
public class ContentResult {

    public Equipment potion;

    public Equipment armoire;

    public ContentGear gear;

    public RealmList<QuestContent> quests;
    public RealmList<Egg> eggs;
    public RealmList<Food> food;
    public RealmList<HatchingPotion> hatchingPotions;

    public RealmList<Pet> pets;

    public RealmList<Mount> mounts;

    public List<Skill> spells;

    public RealmList<Customization> appearances;
    public RealmList<Customization> backgrounds;

    public RealmList<FAQArticle> faq;
    public RealmList<SpecialItem> special;
}

