package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.habitrpg.android.habitica.models.ContentGear;
import com.habitrpg.android.habitica.models.ContentResult;
import com.habitrpg.android.habitica.models.inventory.Customization;
import com.habitrpg.android.habitica.models.FAQArticle;
import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.inventory.Egg;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.HatchingPotion;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.tasks.ItemData;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContentDeserializer implements JsonDeserializer<ContentResult> {

    @Override
    public ContentResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<BaseModel> items = new ArrayList<>();

        ContentResult result = new ContentResult();
        JsonObject object = json.getAsJsonObject();

        result.potion = context.deserialize(object.get("potion"), ItemData.class);
        result.armoire = context.deserialize(object.get("armoire"), ItemData.class);
        result.gear = context.deserialize(object.get("gear"), ContentGear.class);

        items.add(result.potion);
        items.add(result.armoire);
        items.addAll(result.gear.flat);

        result.quests = context.deserialize(object.get("quests"), new TypeToken<List<QuestContent>>() {
        }.getType());
        result.eggs = context.deserialize(object.get("eggs"), new TypeToken<List<Egg>>() {
        }.getType());
        result.food = context.deserialize(object.get("food"), new TypeToken<List<Food>>() {
        }.getType());
        result.hatchingPotions = context.deserialize(object.get("hatchingPotions"), new TypeToken<List<HatchingPotion>>() {
        }.getType());

        items.addAll(result.quests);
        items.addAll(result.eggs);
        items.addAll(result.food);
        items.addAll(result.hatchingPotions);

        result.pets = context.deserialize(object.get("pets"), new TypeToken<Map<String, Pet>>() {
        }.getType());
        result.specialPets = context.deserialize(object.get("specialPets"), new TypeToken<Map<String, Pet>>() {
        }.getType());
        result.premiumPets = context.deserialize(object.get("premiumPets"), new TypeToken<Map<String, Pet>>() {
        }.getType());
        result.questPets = context.deserialize(object.get("questPets"), new TypeToken<Map<String, Pet>>() {
        }.getType());
        result.mounts = context.deserialize(object.get("mounts"), new TypeToken<Map<String, Mount>>() {
        }.getType());
        result.specialMounts = context.deserialize(object.get("specialMounts"), new TypeToken<Map<String, Mount>>() {
        }.getType());
        result.premiumMounts = context.deserialize(object.get("premiumMounts"), new TypeToken<Map<String, Mount>>() {
        }.getType());
        result.questMounts = context.deserialize(object.get("questMounts"), new TypeToken<Map<String, Mount>>() {
        }.getType());

        for (Egg egg : result.eggs) {
            for (HatchingPotion potion : result.hatchingPotions) {
                String key = egg.getKey() + "-" + potion.getKey();
                if (result.pets.containsKey(key)) {
                    result.pets.put(key, this.populatePet(result.pets.get(key), egg, potion));
                }
                if (result.specialPets.containsKey(key)) {
                    result.specialPets.put(key, this.populatePet(result.specialPets.get(key), egg, potion));
                }
                if (result.premiumPets.containsKey(key)) {
                    result.premiumPets.put(key, this.populatePet(result.premiumPets.get(key), egg, potion));
                }
                if (result.questPets.containsKey(key)) {
                    result.questPets.put(key, this.populatePet(result.questPets.get(key), egg, potion));
                }
                if (result.mounts.containsKey(key)) {
                    result.mounts.put(key, this.popupateMount(result.mounts.get(key), egg, potion));
                }
                if (result.specialMounts.containsKey(key)) {
                    result.specialMounts.put(key, this.popupateMount(result.specialMounts.get(key), egg, potion));
                }
                if (result.premiumMounts.containsKey(key)) {
                    result.premiumMounts.put(key, this.popupateMount(result.premiumMounts.get(key), egg, potion));
                }
                if (result.questMounts.containsKey(key)) {
                    result.questMounts.put(key, this.popupateMount(result.questMounts.get(key), egg, potion));
                }
            }
        }

        for (Pet pet : result.pets.values()) {
            pet.setAnimalGroup("pets");
            items.add(pet);
        }
        for (Pet pet : result.specialPets.values()) {
            pet.setAnimalGroup("specialPets");
            items.add(pet);
        }
        for (Pet pet : result.premiumPets.values()) {
            pet.setAnimalGroup("premiumPets");
            items.add(pet);
        }
        for (Pet pet : result.questPets.values()) {
            pet.setAnimalGroup("questPets");
            items.add(pet);
        }

        for (Mount mount : result.mounts.values()) {
            mount.setAnimalGroup("mounts");
            items.add(mount);
        }
        for (Mount mount : result.specialMounts.values()) {
            mount.setAnimalGroup("specialMounts");
            items.add(mount);
        }
        for (Mount mount : result.premiumMounts.values()) {
            mount.setAnimalGroup("premiumMounts");
            items.add(mount);
        }
        for (Mount mount : result.questMounts.values()) {
            mount.setAnimalGroup("questMounts");
            items.add(mount);
        }

        result.spells = context.deserialize(object.get("spells"), new TypeToken<List<Skill>>() {
        }.getType());

        List<String> spellKeys = new ArrayList<>();
        for (Skill skill : result.spells) {
            spellKeys.add(skill.key);
        }
        List<Skill> oldSkills = new Select().from(Skill.class).queryList();
        for (Skill skill : oldSkills) {
            if (!spellKeys.contains(skill.key)) {
                skill.delete();
            }
        }

        result.appearances = context.deserialize(object.get("appearances"), new TypeToken<List<Customization>>() {
        }.getType());
        result.backgrounds = context.deserialize(object.get("backgrounds"), new TypeToken<List<Customization>>() {
        }.getType());

        result.faq = context.deserialize(object.get("faq"), new TypeToken<List<FAQArticle>>() {
        }.getType());

        items.addAll(result.spells);

        items.addAll(result.appearances);
        items.addAll(result.backgrounds);

        items.addAll(result.faq);

        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(items)));
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
