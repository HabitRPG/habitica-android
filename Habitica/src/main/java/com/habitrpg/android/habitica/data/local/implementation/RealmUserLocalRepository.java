package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.UserLocalRepository;
import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.Tag;
import com.habitrpg.android.habitica.models.TutorialStep;
import com.habitrpg.android.habitica.models.user.SpecialItems;
import com.habitrpg.android.habitica.models.user.User;

import java.util.ArrayList;
import java.util.List;

import io.realm.OrderedRealmCollectionSnapshot;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import rx.Observable;


public class RealmUserLocalRepository extends RealmBaseLocalRepository implements UserLocalRepository {

    public RealmUserLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public Observable<User> getUser(String userID) {
        return realm.where(User.class)
                .equalTo("id", userID)
                .findAll()
                .asObservable()
                .filter(realmObject -> realmObject.isLoaded() && realmObject.isValid() && !realmObject.isEmpty())
                .map(users -> users.first());
    }

    @Override
    public void saveUser(User user) {
        realm.executeTransaction(realm1 -> {
            realm1.insertOrUpdate(user);

            if (user.getTags() != null) {
                removeOldTags(user.getId(), user.getTags());
            }
        });
    }

    private void removeOldTags(String userId, List<Tag> onlineTags) {
        OrderedRealmCollectionSnapshot<Tag> tags = realm.where(Tag.class).equalTo("userId", userId).findAll().createSnapshot();

        for (Tag tag : tags) {
            if (!onlineTags.contains(tag)) {
                tag.deleteFromRealm();
            }
        }
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
                .lessThanOrEqualTo("lvl", user.getStats().lvl)
                .findAll()
                .asObservable()
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
        if (ownedItems.size() == 0) {
            ownedItems.add("");
        }
        return realm.where(Skill.class)
                .in("key", ownedItems.toArray(new String[0]))
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }
}
