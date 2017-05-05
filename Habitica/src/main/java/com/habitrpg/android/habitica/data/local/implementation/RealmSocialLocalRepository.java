package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.SocialLocalRepository;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.tasks.Task;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;


public class RealmSocialLocalRepository extends RealmBaseLocalRepository implements SocialLocalRepository {

    public RealmSocialLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public Observable<RealmResults<Group>> getGroups(String type) {
        return realm.where(Group.class)
                .equalTo("type", type)
                .findAllAsync()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Group>> getPublicGuilds() {
        return realm.where(Group.class)
                .equalTo("type", "guild")
                .equalTo("privacy", "public")
                .findAllSorted("memberCount", Sort.DESCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<Group> getGroup(String id) {
        return realm.where(Group.class)
                .equalTo("id", id)
                .findAllAsync()
                .asObservable()
                .filter(group -> group.isLoaded() && group.isValid() && !group.isEmpty())
                .map(groups -> groups.first());
    }

    @Override
    public void saveGroup(Group group) {
        realm.executeTransactionAsync(realm1 -> realm1.insertOrUpdate(group));
    }

    @Override
    public void saveGroups(List<Group> groups) {
        realm.executeTransactionAsync(realm1 -> realm1.insertOrUpdate(groups));
    }
}
