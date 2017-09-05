package com.habitrpg.android.habitica.data;

import android.support.annotation.Nullable;

import com.habitrpg.android.habitica.models.AchievementResult;
import com.habitrpg.android.habitica.models.ContentResult;
import com.habitrpg.android.habitica.models.LeaveChallengeBody;
import com.habitrpg.android.habitica.models.PurchaseValidationRequest;
import com.habitrpg.android.habitica.models.PurchaseValidationResult;
import com.habitrpg.android.habitica.models.SubscriptionValidationRequest;
import com.habitrpg.android.habitica.models.Tag;
import com.habitrpg.android.habitica.models.auth.UserAuthResponse;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.models.members.Member;
import com.habitrpg.android.habitica.models.responses.BuyResponse;
import com.habitrpg.android.habitica.models.responses.ErrorResponse;
import com.habitrpg.android.habitica.models.responses.FeedResponse;
import com.habitrpg.android.habitica.models.responses.HabitResponse;
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult;
import com.habitrpg.android.habitica.models.responses.SkillResponse;
import com.habitrpg.android.habitica.models.responses.Status;
import com.habitrpg.android.habitica.models.responses.TaskDirectionData;
import com.habitrpg.android.habitica.models.responses.UnlockResponse;
import com.habitrpg.android.habitica.models.shops.Shop;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.user.Items;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;
import java.util.Map;

import retrofit2.HttpException;
import rx.Observable;


public interface ApiClient {
    void setLanguageCode(String languageCode);

    Observable<Status> getStatus();

    Observable<ContentResult> getContent();
    Observable<ContentResult> getContent(String language);

    /* user API */

    Observable<User> getUser();

    Observable<User> updateUser(Map<String, Object> updateDictionary);

    Observable<User> registrationLanguage(String registrationLanguage);

    Observable<List<ShopItem>> retrieveInAppRewards();

    Observable<Items> equipItem(String type, String itemKey);

    Observable<BuyResponse> buyItem(String itemKey);

    Observable<Void> purchaseItem(String type, String itemKey);

    Observable<Void> purchaseHourglassItem(String type, String itemKey);

    Observable<Void> purchaseMysterySet(String itemKey);

    Observable<Void> purchaseQuest(String key);
    Observable<Void> validateSubscription(SubscriptionValidationRequest request);

    Observable<User> sellItem(String itemType, String itemKey);

    Observable<FeedResponse> feedPet(String petKey, String foodKey);

    Observable<Items> hatchPet(String eggKey, String hatchingPotionKey);


    Observable<TaskList> getTasks();
    Observable<TaskList> getTasks(String type);
    Observable<TaskList> getTasks(String type, String dueDate);


    Observable<UnlockResponse> unlockPath(String path);

    Observable<Task> getTask(String id);

    Observable<TaskDirectionData> postTaskDirection(String id, String direction);

    Observable<List<String>> postTaskNewPosition(String id, int position);

    Observable<Task> scoreChecklistItem(String taskId, String itemId);

    Observable<Task> createTask(Task item);

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

    Observable<User> revive();

    Observable<SkillResponse> useSkill(String skillName, String targetType, String targetId);

    Observable<SkillResponse> useSkill(String skillName, String targetType);

    Observable<User> changeClass();

    Observable<User> changeClass(String className);

    Observable<User> disableClasses();

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

    Observable<List<Member>> getGroupMembers(String groupId, Boolean includeAllPublicFields);

    Observable<List<Member>> getGroupMembers(String groupId, Boolean includeAllPublicFields, String lastId);

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

    Observable<User> changeCustomDayStart(Map<String, Object> updateObject);

    //Members URL
    Observable<Member> getMember(String memberId);

    Observable<AchievementResult> getMemberAchievements(String memberId);

    Observable<PostChatMessageResult> postPrivateMessage(Map<String, String> messageDetails);

    Observable<Shop> fetchShopInventory(String identifier);

    //Push notifications
    Observable<Void> addPushDevice(Map<String, String> pushDeviceData);

    Observable<Void> deletePushDevice(String regId);

    /* challenges api */

    Observable<List<Challenge>> getUserChallenges();

    Observable<TaskList> getChallengeTasks(String challengeId);

    Observable<Challenge> getChallenge(String challengeId);

    Observable<Challenge> joinChallenge(String challengeId);

    Observable<Void> leaveChallenge(String challengeId, LeaveChallengeBody body);


    Observable<Challenge> createChallenge(Challenge challenge);

    Observable<Task> createChallengeTask(String challengeId, Task task);
    Observable<List<Task>> createChallengeTasks(String challengeId, List<Task> tasks);
    Observable<Challenge> updateChallenge(Challenge challenge);
    Observable<Void> deleteChallenge(String challengeId);

    //DEBUG: These calls only work on a local development server

    Observable<Void> debugAddTenGems();

    // Notifications
    Observable<List> readNotification(String notificationId);

    ErrorResponse getErrorResponse(HttpException throwable);

    void updateAuthenticationCredentials(@Nullable String userID, @Nullable String apiToken);

    boolean hasAuthenticationKeys();

    Observable<User> retrieveUser(boolean withTasks);

    <T> Observable.Transformer<HabitResponse<T>, T> configureApiCallObserver();

    Observable<Equipment> openMysteryItem();

    Observable<Void> runCron();

    Observable<Void> resetAccount();
    Observable<Void> deleteAccount(String password);
}
