package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.TutorialStep;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;

public interface UserLocalRepository extends BaseLocalRepository {

    Observable<User> getUser(String userID);

    void saveUser(User user);

    Observable<RealmResults<TutorialStep>> getTutorialSteps();

    Observable<RealmResults<Skill>> getSkills(User user);

    Observable<RealmResults<Skill>> getSpecialItems(User user);

    Observable<RealmResults<ChatMessage>> getInboxMessages(String userId, String replyToUserID);

    Observable<RealmResults<ChatMessage>> getInboxOverviewList(String userId);
}
