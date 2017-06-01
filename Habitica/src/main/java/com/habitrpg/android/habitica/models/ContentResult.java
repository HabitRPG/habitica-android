package com.habitrpg.android.habitica.models;

import com.habitrpg.android.habitica.models.inventory.Customization;
import com.habitrpg.android.habitica.models.inventory.Egg;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.HatchingPotion;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.tasks.ItemData;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Negue on 15.07.2015.
 */
public class ContentResult {

    public ItemData potion;

    public ItemData armoire;

    public ContentGear gear;

    public List<QuestContent> quests;
    public List<Egg> eggs;
    public List<Food> food;
    public List<HatchingPotion> hatchingPotions;

    public HashMap<String, Pet> pets;
    public HashMap<String, Pet> specialPets;
    public HashMap<String, Pet> premiumPets;
    public HashMap<String, Pet> questPets;

    public HashMap<String, Mount> mounts;
    public HashMap<String, Mount> specialMounts;
    public HashMap<String, Mount> premiumMounts;
    public HashMap<String, Mount> questMounts;

    public List<Skill> spells;

    public List<Customization> appearances;
    public List<Customization> backgrounds;

    public List<FAQArticle> faq;
}

