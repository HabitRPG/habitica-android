package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.ContentLocalRepository;
import com.habitrpg.android.habitica.models.ContentResult;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;


class RealmContentLocalRepository extends RealmBaseLocalRepository implements ContentLocalRepository {

    RealmContentLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public void saveContent(ContentResult result) {
        realm.executeTransactionAsync(realm1 -> {
            List<RealmObject> items = new ArrayList<>();
            items.add(result.potion);
            items.add(result.armoire);
            items.addAll(result.gear.flat);

            items.addAll(result.quests);
            items.addAll(result.eggs);
            items.addAll(result.food);
            items.addAll(result.hatchingPotions);

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

            items.addAll(result.spells);

            items.addAll(result.appearances);
            items.addAll(result.backgrounds);

            items.addAll(result.faq);

            realm1.insertOrUpdate(items);
        });
    }
}
