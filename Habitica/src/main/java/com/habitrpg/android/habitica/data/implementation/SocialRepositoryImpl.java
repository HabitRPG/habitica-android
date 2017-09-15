package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.data.local.SocialLocalRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.AchievementResult;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.models.members.Member;
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

    private final String userId;

    public SocialRepositoryImpl(SocialLocalRepository localRepository, ApiClient apiClient, String userId) {
        super(localRepository, apiClient);
        this.userId = userId;
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
                .doOnNext(chatMessages -> localRepository.saveChatMessages(groupId, chatMessages));
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
    public Observable<Void> flagMessage(ChatMessage chatMessage) {
        if (chatMessage.id == null) {
            return Observable.just(null);
        }
        return apiClient.flagMessage(chatMessage.groupId, chatMessage.id);
    }

    @Override
    public Observable<ChatMessage> likeMessage(ChatMessage chatMessage) {
        if (chatMessage.id == null) {
            return Observable.just(null);
        }
        boolean liked = chatMessage.userLikesMessage(userId);
        localRepository.likeMessage(chatMessage, userId, !liked);
        return apiClient.likeMessage(chatMessage.groupId, chatMessage.id)
                .doOnError(throwable -> localRepository.likeMessage(chatMessage, userId, liked));
    }

    @Override
    public Observable<Void> deleteMessage(ChatMessage chatMessage) {
        return apiClient.deleteMessage(chatMessage.groupId, chatMessage.id)
                .doOnNext(aVoid -> localRepository.deleteMessage(chatMessage.id));
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
        Observable<Group> observable = apiClient.getGroup(id);
        if (!"party".equals(id)) {
            observable = observable.withLatestFrom(localRepository.getGroup(id)
                    .first(), (newGroup, oldGroup) -> {
                newGroup.isMember = oldGroup.isMember;
                return newGroup;
            });
        }
        return observable.map(group -> {
            for (ChatMessage message : group.chat) {
                message.groupId = group.id;
            }
            return group;
        }).doOnNext(localRepository::save);
    }

    @Override
    public Observable<Group> getGroup(String id) {
        return localRepository.getGroup(id);
    }

    @Override
    public Observable<Group> leaveGroup(String id) {
        return apiClient.leaveGroup(id)
                .flatMap(aVoid -> localRepository.getGroup(id).first())
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
        return apiClient.updateGroup(copiedGroup.id, copiedGroup);
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
    public Observable<RealmResults<Member>> getGroupMembers(String id) {
        return localRepository.getGroupMembers(id);
    }

    @Override
    public Observable<List<Member>> retrieveGroupMembers(String id, boolean includeAllPublicFields) {
        return apiClient.getGroupMembers(id, includeAllPublicFields)
                .doOnNext(members -> localRepository.saveGroupMembers(id, members));
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
    public Observable<Member> getMember(String userId) {
        if (userId == null) {
           return Observable.just(null);
        }
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
        return apiClient.cancelQuest(partyId)
                .doOnNext(aVoid -> localRepository.removeQuest(partyId));
    }

    @Override
    public Observable<Quest> abortQuest(String partyId) {
        return apiClient.abortQuest(partyId)
                .doOnNext(aVoid -> localRepository.removeQuest(partyId));
    }

    @Override
    public Observable<Void> rejectGroupInvite(String groupId) {
        return apiClient.rejectQuest(groupId);
    }

    @Override
    public Observable<Quest> forceStartQuest(Group party) {
        return apiClient.forceStartQuest(party.id, localRepository.getUnmanagedCopy(party))
                .doOnNext(aVoid -> localRepository.setQuestActivity(party, true));
    }

    @Override
    public Observable<AchievementResult> getMemberAchievements(String userId) {
        if (userId == null) {
            return Observable.just(null);
        }
        return apiClient.getMemberAchievements(userId);
    }
}
