package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.TagLocalRepository;
import com.habitrpg.android.habitica.models.Tag;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;


public class RealmTagLocalRepository extends RealmBaseLocalRepository implements TagLocalRepository {
    public RealmTagLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public Observable<RealmResults<Tag>> getTags(String userId) {
        return realm.where(Tag.class).equalTo("userId", userId).findAll().asObservable();
    }

    @Override
    public void removeOldTags(List<Tag> onlineTags) {

    }
}
