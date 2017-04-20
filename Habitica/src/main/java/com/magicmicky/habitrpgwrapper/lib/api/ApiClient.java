package com.magicmicky.habitrpgwrapper.lib.api;

import android.support.annotation.Nullable;

import com.habitrpg.android.habitica.ErrorResponse;
import com.magicmicky.habitrpgwrapper.lib.models.AchievementResult;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.ContentResult;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Items;
import com.magicmicky.habitrpgwrapper.lib.models.LeaveChallengeBody;
import com.magicmicky.habitrpgwrapper.lib.models.PostChallenge;
import com.magicmicky.habitrpgwrapper.lib.models.PostChatMessageResult;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationResult;
import com.magicmicky.habitrpgwrapper.lib.models.Quest;
import com.magicmicky.habitrpgwrapper.lib.models.Shop;
import com.magicmicky.habitrpgwrapper.lib.models.Status;
import com.magicmicky.habitrpgwrapper.lib.models.SubscriptionValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.BuyResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.FeedResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.HabitResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.SkillResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.UnlockResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;

public interface ApiClient {
    void setLanguageCode(String languageCode);

    Observable<Status> getStatus();

    Observable<ContentResult> getContent();
    Observable<ContentResult> getContent(String language);

    /* user API */

    Observable<HabitRPGUser> getUser();

    Observable<HabitRPGUser> updateUser(Map<String, Object> updateDictionary);

    Observable<HabitRPGUser> registrationLanguage(String registrationLanguage);

    Observable<List<ItemData>> getInventoryBuyableGear();

    Observable<Items> equipItem(String type, String itemKey);

    Observable<BuyResponse> buyItem(String itemKey);

    Observable<Void> purchaseItem(String type, String itemKey);

    Observable<Void> purchaseHourglassItem(String type, String itemKey);

    Observable<Void> purchaseMysterySet(String itemKey);

    Observable<Void> purchaseQuest(String key);
    Observable<Void> validateSubscription(SubscriptionValidationRequest request);

    Observable<HabitRPGUser> sellItem(String itemType, String itemKey);

    Observable<FeedResponse> feedPet(String petKey, String foodKey);

    Observable<Items> hatchPet(String eggKey, String hatchingPotionKey);


    Observable<TaskList> getTasks();

    Observable<UnlockResponse> unlockPath(String path);

    Observable<Task> getTask(String id);

    Observable<TaskDirectionData> postTaskDirection(String id, String direction);

    Observable<ArrayList<String>> postTaskNewPosition(String id, String position);

    Observable<Task> scoreChecklistItem(String taskId, String itemId);

    Observable<Task> createItem(Task item);

    Observable<List<Task>> createTasks(List<Task> tasks);

    Observable<Task> updateTask(String id, Task item);

    Observable<Void> deleteTask(String id);


    Observable<Tag> createTag(Tag tag);

    Observable<Tag> updateTag(String id, Tag tag);

    Observable<Void> deleteTag(String id);

    Observable<UserAuthResponse> registerUser(String username, String email, String password, String confirmPassword);

    Observable<UserAuthResponse> connectUser(String username, String password);

    Observable<UserAuthResponse> connectSocial(String network, String userId, String accessToken);
    Observable<Boolean> sleep();

    Observable<HabitRPGUser> revive();

    Observable<SkillResponse> useSkill(String skillName, String targetType, String targetId);

    Observable<SkillResponse> useSkill(String skillName, String targetType);

    Observable<HabitRPGUser> changeClass();

    Observable<HabitRPGUser> changeClass(String className);

    Observable<HabitRPGUser> disableClasses();

    Observable<Void> markPrivateMessagesRead();

    /* Group API */

    Observable<List<Group>> listGroups(String type);

    Observable<Group> getGroup(String groupId);

    Observable<Void> updateGroup(String id, Group item);

    Observable<List<ChatMessage>> listGroupChat(String groupId);

    Observable<Group> joinGroup(String groupId);

    Observable<Void> leaveGroup(String groupId);

    Observable<PostChatMessageResult> postGroupChat(String groupId, Map<String, String> message);

    Observable<Void> deleteMessage(String groupId, String messageId);

    Observable<List<HabitRPGUser>> getGroupMembers(String groupId, Boolean includeAllPublicFields);

    Observable<List<HabitRPGUser>> getGroupMembers(String groupId, Boolean includeAllPublicFields, String lastId);

    // Like returns the full chat list
    Observable<ChatMessage> likeMessage(String groupId, String mid);

    Observable<Void> flagMessage(String groupId, String mid);

    Observable<Void> seenMessages(String groupId);

    Observable<Void> inviteToGroup(String groupId, Map<String, Object> inviteData);

    Observable<Void> rejectGroupInvite(String groupId);

    Observable<Void> acceptQuest(String groupId);

    Observable<Void> rejectQuest(String groupId);

    Observable<Void> cancelQuest(String groupId);

    Observable<Quest> forceStartQuest(String groupId, Group group);

    Observable<Quest> inviteToQuest(String groupId,String questKey);

    Observable<Quest> abortQuest(String groupId);

    Observable<Void> leaveQuest(String groupId);

    Observable<PurchaseValidationResult> validatePurchase(PurchaseValidationRequest request);

    Observable<HabitRPGUser> changeCustomDayStart(Map<String, Object> updateObject);

    //Members URL
    Observable<HabitRPGUser> getMember(String memberId);

    Observable<AchievementResult> getMemberAchievements(String memberId);

    Observable<PostChatMessageResult> postPrivateMessage(Map<String, String> messageDetails);

    Observable<Shop> fetchShopInventory(String identifier);

    //Push notifications
    Observable<Void> addPushDevice(Map<String, String> pushDeviceData);

    Observable<Void> deletePushDevice(String regId);

    /* challenges api */

    Observable<ArrayList<Challenge>> getUserChallenges();

    Observable<TaskList> getChallengeTasks(String challengeId);

    Observable<Challenge> getChallenge(String challengeId);

    Observable<Challenge> joinChallenge(String challengeId);

    Observable<Void> leaveChallenge(String challengeId, LeaveChallengeBody body);

    Observable<Challenge> createChallenge(PostChallenge challenge);
    Observable<Void> createChallengeTasks(String challengeId, List<Task> tasks);
    Observable<Challenge> updateChallenge(PostChallenge challenge);
    Observable<Void> deleteChallenge(String challengeId);

    //DEBUG: These calls only work on a local development server

    Observable<Void> debugAddTenGems();

    // Notifications
    Observable<Void> readNotificaiton(String notificationId);

    ErrorResponse getErrorResponse(HttpException throwable);

    void updateAuthenticationCredentials(@Nullable String userID, @Nullable String apiToken);

    boolean hasAuthenticationKeys();

    Observable<HabitRPGUser> retrieveUser(boolean withTasks);

    <T> Observable.Transformer<HabitResponse<T>, T> configureApiCallObserver();

    Observable<ItemData> openMysteryItem();
}
