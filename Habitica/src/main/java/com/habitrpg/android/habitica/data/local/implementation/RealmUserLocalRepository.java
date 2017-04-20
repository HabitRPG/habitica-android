package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.UserLocalRepository;
import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.TutorialStep;
import com.habitrpg.android.habitica.models.user.SpecialItems;
import com.habitrpg.android.habitica.models.user.User;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;


public class RealmUserLocalRepository extends RealmBaseLocalRepository implements UserLocalRepository {

    public RealmUserLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public Observable<User> getUser(String userID) {
        return realm.where(User.class).equalTo("id", userID).findFirstAsync().asObservable()
                .filter(realmObject -> realmObject.isLoaded())
                .cast(User.class);
    }

    @Override
    public void saveUser(User user) {
        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(user));
    }

    @Override
    public Observable<RealmResults<TutorialStep>> getTutorialSteps() {
        return realm.where(TutorialStep.class).findAll().asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Skill>> getSkills(User user) {
        return realm.where(Skill.class)
                .equalTo("habitClass", user.getStats().getHabitClass())
                .equalTo("lvl", user.getStats().lvl)
                .findAll().asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Skill>> getSpecialItems(User user) {
        SpecialItems specialItems = user.getItems().getSpecial();
        List<String> ownedItems = new ArrayList<>();
        if (specialItems != null) {
            if (specialItems.getSnowball() > 0) {
                ownedItems.add("snowball");
            }

            if (specialItems.getShinySeed() > 0) {
                ownedItems.add("shinySeed");
            }

            if (specialItems.getSeafoam() > 0) {
                ownedItems.add("seafoam");
            }

            if (specialItems.getSpookySparkles() > 0) {
                ownedItems.add("spookySparkles");
            }
        }
        return realm.where(Skill.class)
                .in("key", (String[]) ownedItems.toArray())
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }
}
