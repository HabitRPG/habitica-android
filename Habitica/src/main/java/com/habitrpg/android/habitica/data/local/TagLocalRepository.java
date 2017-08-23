package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.Tag;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;

public interface TagLocalRepository extends BaseLocalRepository {
    Observable<RealmResults<Tag>> getTags(String userId);

    void removeOldTags(List<Tag> onlineTags, String userID);
}
