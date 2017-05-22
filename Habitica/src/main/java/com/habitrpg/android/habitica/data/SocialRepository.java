package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.responses.PostChatMessageResult;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import rx.Observable;

public interface SocialRepository extends BaseRepository {
    Observable<List<ChatMessage>> retrieveGroupChat(String groupId);
    Observable<RealmResults<ChatMessage>> getGroupChat(String groupId);

    void markMessagesSeen(String seenGroupId);

    Observable<Void> flagMessage(String groupId, String id);

    Observable<ChatMessage> likeMessage(String groupId, String id);

    Observable<Void> deleteMessage(String groupId, String id);

    Observable<PostChatMessageResult> postGroupChat(String groupId, HashMap<String, String> messageObject);
    Observable<PostChatMessageResult> postGroupChat(String groupId, String message);

    Observable<Group> retrieveGroup(String id);
    Observable<Group> getGroup(String id);

    Observable<Group> leaveGroup(String id);

    Observable<Group> joinGroup(String id);

    Observable<Void> updateGroup(Group group, String name, String description, String leader, String privacy);

    Observable<List<Group>> retrieveGroups(String type);
    Observable<RealmResults<Group>> getGroups(String type);
    Observable<RealmResults<Group>> getPublicGuilds();

    Observable<PostChatMessageResult> postPrivateMessage(HashMap<String, String> messageObject);
    Observable<PostChatMessageResult> postPrivateMessage(String recipientId, String message);


    Observable<RealmResults<User>> getGroupMembers(String id);
    Observable<List<User>> retrieveGroupMembers(String id, boolean includeAllPublicFields);

    Observable<Void> inviteToGroup(String id, Map<String, Object> inviteData);

    Observable<List<Challenge>> getUserChallenges();

    Observable<User> getMember(String userId);

    Observable<Void> markPrivateMessagesRead(User user);

    Observable<RealmResults<Group>> getUserGroups();
}
