package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.ContentLocalRepository;
import com.habitrpg.android.habitica.models.ContentResult;

import io.realm.Realm;


class RealmContentLocalRepository extends RealmBaseLocalRepository implements ContentLocalRepository {

    RealmContentLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public void saveContent(ContentResult result) {
        realm.executeTransactionAsync(realm1 -> {
            realm1.insertOrUpdate(result.potion);
            realm1.insertOrUpdate(result.armoire);
            realm1.insertOrUpdate(result.gear.flat);

            realm1.insertOrUpdate(result.quests);
            realm1.insertOrUpdate(result.eggs);
            realm1.insertOrUpdate(result.food);
            realm1.insertOrUpdate(result.hatchingPotions);


            realm1.insertOrUpdate(result.pets);
            realm1.insertOrUpdate(result.mounts);

            realm1.insertOrUpdate(result.spells);
            realm1.insertOrUpdate(result.appearances);
            realm1.insertOrUpdate(result.backgrounds);
            realm1.insertOrUpdate(result.faq);
        });
    }
}
