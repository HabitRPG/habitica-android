package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.SocialRepository;
import com.habitrpg.android.habitica.data.local.SocialLocalRepository;
import com.habitrpg.android.habitica.helpers.ReactiveErrorHandler;
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.schedulers.Schedulers;


public class SocialRepositoryImpl extends BaseRepositoryImpl<SocialLocalRepository> implements SocialRepository {

    public SocialRepositoryImpl(SocialLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<List<ChatMessage>> retrieveGroupChat(String groupId) {
        return apiClient.listGroupChat(groupId)
                .flatMap(Observable::from)
                .map(chatMessage -> {
                    chatMessage.parsedText = MarkdownParser.parseMarkdown(chatMessage.text);
                    return chatMessage;
                })
                .toList()
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<ChatMessage>> getGroupChat(String groupId) {
        return retrieveGroupChat(groupId);
    }

    @Override
    public void markMessagesSeen(String seenGroupId) {
        apiClient.seenMessages(seenGroupId).subscribe(aVoid -> {}, ReactiveErrorHandler.handleEmptyError());
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
        return apiClient.deleteMessage(groupId, id);
    }

    @Override
    public Observable<PostChatMessageResult> postGroupChat(String groupId, HashMap<String, String> messageObject) {
        return apiClient.postGroupChat(groupId, messageObject);
    }

    @Override
    public Observable<PostChatMessageResult> postGroupChat(String groupId, String message) {
        HashMap<String, String> messageObject = new HashMap<>();
        messageObject.put("message", message);
        return postGroupChat(groupId, messageObject);
    }

    @Override
    public Observable<Group> retrieveGroup(String id) {
        return apiClient.getGroup(id);
    }

    @Override
    public Observable<Group> getGroup(String id) {
        return retrieveGroup(id);
    }

    @Override
    public Observable<Void> leaveGroup(String id) {
        return apiClient.leaveGroup(id);
    }

    @Override
    public Observable<Group> joinGroup(String id) {
        return apiClient.joinGroup(id);
    }

    @Override
    public Observable<Void> updateGroup(Group group) {
        return apiClient.updateGroup(group.id, group);
    }

    @Override
    public Observable<List<Group>> retrieveGroups(String type) {
        return apiClient.listGroups(type);
    }

    @Override
    public Observable<List<Group>> getGroups(String type) {
        return retrieveGroups(type);
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
    public Observable<List<User>> getGroupMembers(String id, boolean includeAllPublicFields) {
        return apiClient.getGroupMembers(id, includeAllPublicFields);
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
    public Observable<Challenge> getChallenge(String challengeId) {
        return localRepository.getChallenge(challengeId);
    }

    @Override
    public Observable<List<Challenge>> getChallenges() {
        return localRepository.getChallenges();
    }

    @Override
    public Observable<List<Challenge>> getUserChallenges(String userId) {
        return localRepository.getUserChallenges(userId);
    }
}
