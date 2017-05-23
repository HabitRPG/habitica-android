package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;
import rx.functions.Action1;

public interface SocialLocalRepository extends BaseLocalRepository {
    Observable<RealmResults<Group>> getGroups(String type);
    Observable<RealmResults<Group>> getPublicGuilds();

    Observable<Group> getGroup(String id);

    Observable<RealmResults<Group>> getUserGroups();

    Observable<RealmResults<ChatMessage>> getGroupChat(String groupId);

    void deleteMessage(String id);

    Observable<RealmResults<User>> getGroupMembers(String partyId);

    void updateRSVPNeeded(User user, boolean newValue);
}
