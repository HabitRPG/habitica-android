package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.data.local.SocialLocalRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.inventory.Quest;
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


public class SocialRepositoryImpl extends BaseRepositoryImpl<SocialLocalRepository> implements SocialRepository {

    public SocialRepositoryImpl(SocialLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<List<ChatMessage>> retrieveGroupChat(String groupId) {
        return apiClient.listGroupChat(groupId)
                .flatMap(Observable::from)
                .map(chatMessage -> {
                    chatMessage.groupId = groupId;
                    return chatMessage;
                })
                .toList()
                .doOnNext(localRepository::save);
    }

    @Override
    public Observable<RealmResults<ChatMessage>> getGroupChat(String groupId) {
        return localRepository.getGroupChat(groupId);
    }

    @Override
    public void markMessagesSeen(String seenGroupId) {
        apiClient.seenMessages(seenGroupId).subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
    }

    @Override
    public Observable<Void> flagMessage(String groupId, String id) {
        return apiClient.flagMessage(groupId, id);
    }

    @Override
    public Observable<ChatMessage> likeMessage(String groupId, String id) {
        return apiClient.likeMessage(groupId, id);
    }

    @Override
    public Observable<Void> deleteMessage(String groupId, String id) {
        return apiClient.deleteMessage(groupId, id)
                .doOnNext(aVoid -> localRepository.deleteMessage(id));
    }

    @Override
    public Observable<PostChatMessageResult> postGroupChat(String groupId, HashMap<String, String> messageObject) {
        return apiClient.postGroupChat(groupId, messageObject)
                .map(postChatMessageResult -> {
                    postChatMessageResult.message.groupId = groupId;
                    return postChatMessageResult;
                })
                .doOnNext(postChatMessageResult -> localRepository.save(postChatMessageResult.message));
    }

    @Override
    public Observable<PostChatMessageResult> postGroupChat(String groupId, String message) {
        HashMap<String, String> messageObject = new HashMap<>();
        messageObject.put("message", message);
        return postGroupChat(groupId, messageObject);
    }

    @Override
    public Observable<Group> retrieveGroup(String id) {
        return apiClient.getGroup(id)
                .map(group -> {
                    for (ChatMessage message : group.chat) {
                        message.groupId = group.id;
                    }
                    return group;
                })
                .doOnNext(localRepository::save);
    }

    @Override
    public Observable<Group> getGroup(String id) {
        return localRepository.getGroup(id);
    }

    @Override
    public Observable<Group> leaveGroup(String id) {
        return apiClient.leaveGroup(id)
                .flatMap(aVoid -> localRepository.getGroup(id))
                .doOnNext(group -> localRepository.executeTransaction(realm -> group.isMember = false));
    }

    @Override
    public Observable<Group> joinGroup(String id) {
        return apiClient.joinGroup(id)
                .doOnNext(group -> {
                    group.isMember = true;
                    localRepository.save(group);
                });
    }

    @Override
    public Observable<Void> updateGroup(Group group, String name, String description, String leader, String privacy) {
        Group copiedGroup = localRepository.getUnmanagedCopy(group);
        copiedGroup.name = name;
        copiedGroup.description = description;
        copiedGroup.leaderID = leader;
        copiedGroup.privacy = privacy;
        localRepository.save(copiedGroup);
        return apiClient.updateGroup(group.id, group);
    }

    @Override
    public Observable<List<Group>> retrieveGroups(String type) {
        return apiClient.listGroups(type)
                .doOnNext(groups -> {
                    if ("guilds".equals(type)) {
                        for (Group guild : groups) {
                            guild.isMember = true;
                        }
                    }
                    localRepository.save(groups);
                });
    }

    @Override
    public Observable<RealmResults<Group>> getGroups(String type) {
        return localRepository.getGroups(type);
    }

    @Override
    public Observable<RealmResults<Group>> getPublicGuilds() {
        return localRepository.getPublicGuilds();
    }

    @Override
    public Observable<PostChatMessageResult> postPrivateMessage(HashMap<String, String> messageObject) {
        return apiClient.postPrivateMessage(messageObject);
    }

    @Override
    public Observable<PostChatMessageResult> postPrivateMessage(String recipientId, String message) {
        HashMap<String, String> messageObject = new HashMap<>();
        messageObject.put("message", message);
        messageObject.put("toUserId", recipientId);
        return postPrivateMessage(messageObject);
    }

    @Override
    public Observable<RealmResults<User>> getGroupMembers(String id) {
        return localRepository.getGroupMembers(id);
    }

    @Override
    public Observable<List<User>> retrieveGroupMembers(String id, boolean includeAllPublicFields) {
        return apiClient.getGroupMembers(id, includeAllPublicFields)
                .doOnNext(localRepository::save);
    }

    @Override
    public Observable<Void> inviteToGroup(String id, Map<String, Object> inviteData) {
        return apiClient.inviteToGroup(id, inviteData);
    }

    @Override
    public Observable<List<Challenge>> getUserChallenges() {
        return apiClient.getUserChallenges();
    }

    @Override
    public Observable<User> getMember(String userId) {
        return apiClient.getMember(userId);
    }

    @Override
    public Observable<Void> markPrivateMessagesRead(User user) {
        return apiClient.markPrivateMessagesRead()
                .doOnNext(aVoid -> localRepository.executeTransaction(realm -> user.getInbox().setNewMessages(0)));
    }

    @Override
    public Observable<RealmResults<Group>> getUserGroups() {
        return localRepository.getUserGroups();
    }

    @Override
    public Observable<Void> acceptQuest(User user, String partyId) {
        return apiClient.acceptQuest(partyId)
                .doOnNext(aVoid -> localRepository.updateRSVPNeeded(user, false));
    }

    @Override
    public Observable<Void> rejectQuest(User user, String partyId) {
        return apiClient.rejectQuest(partyId)
                .doOnNext(aVoid -> localRepository.updateRSVPNeeded(user, false));
    }

    @Override
    public Observable<Void> leaveQuest(String partyId) {
        return apiClient.leaveQuest(partyId);
    }

    @Override
    public Observable<Void> cancelQuest(String partyId) {
        return apiClient.cancelQuest(partyId);
    }

    @Override
    public Observable<Quest> abortQuest(String partyId) {
        return apiClient.abortQuest(partyId);
    }

    @Override
    public Observable<Void> rejectGroupInvite(String groupId) {
        return apiClient.rejectQuest(groupId);
    }

    @Override
    public Observable<Quest> forceStartQuest(Group party) {
        return apiClient.forceStartQuest(party.id, party);
    }
}
