package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.magicmicky.habitrpgwrapper.lib.models.ContentGear;
import com.magicmicky.habitrpgwrapper.lib.models.ContentResult;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.FAQArticle;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Egg;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Food;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.HatchingPotion;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Mount;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Pet;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ContentDeserializer implements JsonDeserializer<ContentResult> {


    @Override
    public ContentResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<BaseModel> items = new ArrayList<>();

        ContentResult result = new ContentResult();
        JsonObject object = json.getAsJsonObject();

        result.potion = context.deserialize(object.get("potion"), ItemData.class);
        result.armoire = context.deserialize(object.get("armoire"), ItemData.class);
        result.gear = context.deserialize(object.get("gear"), ContentGear.class);

        result.quests = context.deserialize(object.get("quests"), new TypeToken<List<QuestContent>>() {}.getType());
        result.eggs = context.deserialize(object.get("eggs"), new TypeToken<List<Egg>>() {}.getType());
        result.food = context.deserialize(object.get("food"), new TypeToken<List<Food>>() {}.getType());
        result.hatchingPotions = context.deserialize(object.get("hatchingPotions"), new TypeToken<List<HatchingPotion>>() {}.getType());

        items.addAll(result.quests);
        items.addAll(result.eggs);
        items.addAll(result.food);
        items.addAll(result.hatchingPotions);

        result.pets = context.deserialize(object.get("pets"), new TypeToken<List<Pet>>() {}.getType());
        result.specialPets = context.deserialize(object.get("specialPets"), new TypeToken<List<Pet>>() {}.getType());
        result.premiumPets = context.deserialize(object.get("premiumPets"), new TypeToken<List<Pet>>() {}.getType());
        result.questPets = context.deserialize(object.get("questPets"), new TypeToken<List<Pet>>() {}.getType());

        for (Pet pet : result.pets) {
            pet.setAnimalGroup("pets");
            items.add(pet);
        }
        for (Pet pet : result.specialPets) {
            pet.setAnimalGroup("specialPets");
            items.add(pet);
        }
        for (Pet pet : result.premiumPets) {
            pet.setAnimalGroup("premiumPets");
            items.add(pet);
        }
        for (Pet pet : result.questPets) {
            pet.setAnimalGroup("questPets");
            items.add(pet);
        }

        result.mounts = context.deserialize(object.get("mounts"), new TypeToken<List<Mount>>() {}.getType());
        result.specialMounts = context.deserialize(object.get("specialMounts"), new TypeToken<List<Mount>>() {}.getType());
        result.questMounts = context.deserialize(object.get("questMounts"), new TypeToken<List<Mount>>() {}.getType());

        for (Mount mount : result.mounts) {
            mount.setAnimalGroup("mounts");
            items.add(mount);
        }
        for (Mount mount : result.specialMounts) {
            mount.setAnimalGroup("specialMounts");
            items.add(mount);
        }
        for (Mount mount : result.questMounts) {
            mount.setAnimalGroup("questMounts");
            items.add(mount);
        }

        result.spells = context.deserialize(object.get("spells"), new TypeToken<List<Skill>>() {}.getType());

        result.appearances = context.deserialize(object.get("appearances"), new TypeToken<List<Customization>>() {}.getType());
        result.backgrounds = context.deserialize(object.get("backgrounds"), new TypeToken<List<Customization>>() {}.getType());

        result.faq = context.deserialize(object.get("faq"), new TypeToken<List<FAQArticle>>() {}.getType());

        items.addAll(result.spells);

        items.addAll(result.appearances);
        items.addAll(result.backgrounds);

        items.addAll(result.faq);

        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(items)));
        return result;
    }
}
