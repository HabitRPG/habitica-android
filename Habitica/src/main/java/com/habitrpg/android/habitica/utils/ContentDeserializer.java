package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.habitrpg.android.habitica.data.CustomizationRepository;
import com.habitrpg.android.habitica.models.ContentGear;
import com.habitrpg.android.habitica.models.ContentResult;
import com.habitrpg.android.habitica.models.FAQArticle;
import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.inventory.Customization;
import com.habitrpg.android.habitica.models.inventory.Egg;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.HatchingPotion;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.realm.RealmList;
import io.realm.RealmObject;

public class ContentDeserializer implements JsonDeserializer<ContentResult> {

    @Inject
    CustomizationRepository customizationRepository;

    @Override
    public ContentResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        ContentResult result = new ContentResult();
        JsonObject object = json.getAsJsonObject();

        result.potion = context.deserialize(object.get("potion"), Equipment.class);
        result.armoire = context.deserialize(object.get("armoire"), Equipment.class);
        result.gear = context.deserialize(object.get("gear"), ContentGear.class);

        result.quests = context.deserialize(object.get("quests"), new TypeToken<RealmList<QuestContent>>() {
        }.getType());
        result.eggs = context.deserialize(object.get("eggs"), new TypeToken<RealmList<Egg>>() {
        }.getType());
        result.food = context.deserialize(object.get("food"), new TypeToken<RealmList<Food>>() {
        }.getType());
        result.hatchingPotions = context.deserialize(object.get("hatchingPotions"), new TypeToken<RealmList<HatchingPotion>>() {
        }.getType());

        Map<String, Pet> pets = context.deserialize(object.get("pets"), new TypeToken<Map<String, Pet>>() {
        }.getType());
        Map<String, Pet> specialPets = context.deserialize(object.get("specialPets"), new TypeToken<Map<String, Pet>>() {
        }.getType());
        Map<String, Pet> premiumPets = context.deserialize(object.get("premiumPets"), new TypeToken<Map<String, Pet>>() {
        }.getType());
        Map<String, Pet> questPets = context.deserialize(object.get("questPets"), new TypeToken<Map<String, Pet>>() {
        }.getType());
        Map<String, Mount> mounts = context.deserialize(object.get("mounts"), new TypeToken<Map<String, Mount>>() {
        }.getType());
        Map<String, Mount> specialMounts = context.deserialize(object.get("specialMounts"), new TypeToken<Map<String, Mount>>() {
        }.getType());
        Map<String, Mount> premiumMounts = context.deserialize(object.get("premiumMounts"), new TypeToken<Map<String, Mount>>() {
        }.getType());
        Map<String, Mount> questMounts = context.deserialize(object.get("questMounts"), new TypeToken<Map<String, Mount>>() {
        }.getType());

        for (Egg egg : result.eggs) {
            for (HatchingPotion potion : result.hatchingPotions) {
                String key = egg.getKey() + "-" + potion.getKey();
                if (pets.containsKey(key)) {
                    pets.put(key, this.populatePet(pets.get(key), egg, potion));
                }
                if (specialPets.containsKey(key)) {
                    specialPets.put(key, this.populatePet(specialPets.get(key), egg, potion));
                }
                if (premiumPets.containsKey(key)) {
                    premiumPets.put(key, this.populatePet(premiumPets.get(key), egg, potion));
                }
                if (questPets.containsKey(key)) {
                    questPets.put(key, this.populatePet(questPets.get(key), egg, potion));
                }
                if (mounts.containsKey(key)) {
                    mounts.put(key, this.popupateMount(mounts.get(key), egg, potion));
                }
                if (specialMounts.containsKey(key)) {
                    specialMounts.put(key, this.popupateMount(specialMounts.get(key), egg, potion));
                }
                if (premiumMounts.containsKey(key)) {
                    premiumMounts.put(key, this.popupateMount(premiumMounts.get(key), egg, potion));
                }
                if (questMounts.containsKey(key)) {
                    questMounts.put(key, this.popupateMount(questMounts.get(key), egg, potion));
                }
            }
        }

        result.pets = new RealmList<>();
        result.pets.addAll(pets.values());
        result.pets.addAll(specialPets.values());
        result.pets.addAll(premiumPets.values());
        result.pets.addAll(questPets.values());

        result.mounts = new RealmList<>();
        result.mounts.addAll(mounts.values());
        result.mounts.addAll(specialMounts.values());
        result.mounts.addAll(premiumMounts.values());
        result.mounts.addAll(questMounts.values());

        result.spells = context.deserialize(object.get("spells"), new TypeToken<List<Skill>>() {
        }.getType());

        List<String> spellKeys = new ArrayList<>();
        for (Skill skill : result.spells) {
            spellKeys.add(skill.key);
        }
        /*List<Skill> oldSkills = new Select().from(Skill.class).queryList();
        for (Skill skill : oldSkills) {
            if (!spellKeys.contains(skill.key)) {
                skill.delete();
            }
        }*/

        result.appearances = context.deserialize(object.get("appearances"), new TypeToken<RealmList<Customization>>() {
        }.getType());
        result.backgrounds = context.deserialize(object.get("backgrounds"), new TypeToken<RealmList<Customization>>() {
        }.getType());

        result.faq = context.deserialize(object.get("faq"), new TypeToken<RealmList<FAQArticle>>() {
        }.getType());

        return result;
    }

    private Mount popupateMount(Mount mount, Egg egg, HatchingPotion potion) {
        mount.setAnimalText(egg.getMountText());
        mount.setColorText(potion.getText());
        mount.setLimited(potion.getLimited());
        mount.setPremium(potion.getPremium());
        return mount;
    }

    private Pet populatePet(Pet pet, Egg egg, HatchingPotion potion) {
        pet.setAnimalText(egg.getText());
        pet.setColorText(potion.getText());
        pet.setLimited(potion.getLimited());
        pet.setPremium(potion.getPremium());
        return pet;
    }
}
