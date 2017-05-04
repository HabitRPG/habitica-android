package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.social.Group;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;

public interface SocialLocalRepository extends BaseLocalRepository {
    Observable<RealmResults<Group>> getGroups(String type);
}
