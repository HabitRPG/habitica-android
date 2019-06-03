package com.habitrpg.android.habitica.api;

import com.habitrpg.android.habitica.models.Achievement;
import com.habitrpg.android.habitica.models.ContentResult;
import com.habitrpg.android.habitica.models.LeaveChallengeBody;
import com.habitrpg.android.habitica.models.PurchaseValidationRequest;
import com.habitrpg.android.habitica.models.PurchaseValidationResult;
import com.habitrpg.android.habitica.models.SubscriptionValidationRequest;
import com.habitrpg.android.habitica.models.Tag;
import com.habitrpg.android.habitica.models.WorldState;
import com.habitrpg.android.habitica.models.auth.UserAuth;
import com.habitrpg.android.habitica.models.auth.UserAuthResponse;
import com.habitrpg.android.habitica.models.auth.UserAuthSocial;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.models.members.Member;
import com.habitrpg.android.habitica.models.responses.BuyResponse;
import com.habitrpg.android.habitica.models.responses.FeedResponse;
import com.habitrpg.android.habitica.models.responses.HabitResponse;
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult;
import com.habitrpg.android.habitica.models.responses.SkillResponse;
import com.habitrpg.android.habitica.models.responses.Status;
import com.habitrpg.android.habitica.models.responses.TaskDirectionData;
import com.habitrpg.android.habitica.models.responses.UnlockResponse;
import com.habitrpg.android.habitica.models.responses.VerifyUsernameResponse;
import com.habitrpg.android.habitica.models.shops.Shop;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.social.FindUsernameResult;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.user.Items;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;


/**
 * Created by MagicMicky on 10/06/2014.
 */
public interface ApiService {
    @GET("status")
    Flowable<HabitResponse<Status>> getStatus();

    @GET("content")
    Flowable<HabitResponse<ContentResult>> getContent(@Query("language") String language);

    /* user API */

    @GET("user/")
    Flowable<HabitResponse<User>> getUser();

    @PUT("user/")
    Flowable<HabitResponse<User>> updateUser(@Body Map<String, Object> updateDictionary);

    @PUT("user/")
    Flowable<HabitResponse<User>> registrationLanguage(@Header("Accept-Language") String registrationLanguage);


    @GET("inbox/messages")
    Flowable<HabitResponse<List<ChatMessage>>> getInboxMessages();

    @GET("user/in-app-rewards")
    Flowable<HabitResponse<List<ShopItem>>> retrieveInAppRewards();
    @GET("user/inventory/buy")
    Flowable<HabitResponse<List<ShopItem>>> retrieveOldGearRewards();

    @POST("user/equip/{type}/{key}")
    Flowable<HabitResponse<Items>> equipItem(@Path("type") String type, @Path("key") String itemKey);

    @POST("user/buy/{key}")
    Flowable<HabitResponse<BuyResponse>> buyItem(@Path("key") String itemKey);

    @POST("user/purchase/{type}/{key}")
    Flowable<HabitResponse<Void>> purchaseItem(@Path("type") String type, @Path("key") String itemKey);

    @POST("user/purchase-hourglass/{type}/{key}")
    Flowable<HabitResponse<Void>> purchaseHourglassItem(@Path("type") String type, @Path("key") String itemKey);

    @POST("user/buy-mystery-set/{key}")
    Flowable<HabitResponse<Void>> purchaseMysterySet(@Path("key") String itemKey);

    @POST("user/buy-quest/{key}")
    Flowable<HabitResponse<Void>> purchaseQuest(@Path("key") String key);

    @POST("user/sell/{type}/{key}")
    Flowable<HabitResponse<User>> sellItem(@Path("type") String itemType, @Path("key") String itemKey);

    @POST("user/feed/{pet}/{food}")
    Flowable<HabitResponse<FeedResponse>> feedPet(@Path("pet") String petKey, @Path("food") String foodKey);

    @POST("user/hatch/{egg}/{hatchingPotion}")
    Flowable<HabitResponse<Items>> hatchPet(@Path("egg") String eggKey, @Path("hatchingPotion") String hatchingPotionKey);


    @GET("tasks/user")
    Flowable<HabitResponse<TaskList>> getTasks();

    @GET("tasks/user")
    Flowable<HabitResponse<TaskList>> getTasks(@Query("type") String type);

    @GET("tasks/user")
    Flowable<HabitResponse<TaskList>> getTasks(@Query("type") String type, @Query("dueDate") String dueDate);


    @POST("user/unlock")
    Flowable<HabitResponse<UnlockResponse>> unlockPath(@Query("path") String path);

    @GET("tasks/{id}")
    Flowable<HabitResponse<Task>> getTask(@Path("id") String id);

    @POST("tasks/{id}/score/{direction}")
    Flowable<HabitResponse<TaskDirectionData>> postTaskDirection(@Path("id") String id, @Path("direction") String direction);

    @POST("tasks/{id}/move/to/{position}")
    Flowable<HabitResponse<List<String>>> postTaskNewPosition(@Path("id") String id, @Path("position") int position);

    @POST("tasks/{taskId}/checklist/{itemId}/score")
    Flowable<HabitResponse<Task>> scoreChecklistItem(@Path("taskId") String taskId, @Path("itemId") String itemId);

    @POST("tasks/user")
    Flowable<HabitResponse<Task>> createTask(@Body Task item);

    @POST("tasks/user")
    Flowable<HabitResponse<List<Task>>> createTasks(@Body List<Task> tasks);

    @PUT("tasks/{id}")
    Flowable<HabitResponse<Task>> updateTask(@Path("id") String id, @Body Task item);

    @DELETE("tasks/{id}")
    Flowable<HabitResponse<Void>> deleteTask(@Path("id") String id);


    @POST("tags")
    Flowable<HabitResponse<Tag>> createTag(@Body Tag tag);

    @PUT("tags/{id}")
    Flowable<HabitResponse<Tag>> updateTag(@Path("id") String id, @Body Tag tag);

    @DELETE("tags/{id}")
    Flowable<HabitResponse<Void>> deleteTag(@Path("id") String id);

    @POST("user/auth/local/register")
    Flowable<HabitResponse<UserAuthResponse>> registerUser(@Body UserAuth auth);

    @POST("user/auth/local/login")
    Flowable<HabitResponse<UserAuthResponse>> connectLocal(@Body UserAuth auth);

    @POST("user/auth/social")
    Flowable<HabitResponse<UserAuthResponse>> connectSocial(@Body UserAuthSocial auth);


    @POST("user/sleep")
    Flowable<HabitResponse<Boolean>> sleep();

    @POST("user/revive")
    Flowable<HabitResponse<User>> revive();

    @POST("user/class/cast/{skill}")
    Flowable<HabitResponse<SkillResponse>> useSkill(@Path("skill") String skillName, @Query("targetType") String targetType, @Query("targetId") String targetId);

    @POST("user/class/cast/{skill}")
    Flowable<HabitResponse<SkillResponse>> useSkill(@Path("skill") String skillName, @Query("targetType") String targetType);

    @POST("user/change-class")
    Flowable<HabitResponse<User>> changeClass();

    @POST("user/change-class")
    Flowable<HabitResponse<User>> changeClass(@Query("class") String className);

    @POST("user/disable-classes")
    Flowable<HabitResponse<User>> disableClasses();

    @POST("user/mark-pms-read")
    Flowable<Void> markPrivateMessagesRead();



    /* Group API */

    @GET("groups")
    Flowable<HabitResponse<List<Group>>> listGroups(@Query("type") String type);

    @GET("groups/{gid}")
    Flowable<HabitResponse<Group>> getGroup(@Path("gid") String groupId);

    @POST("groups")
    Flowable<HabitResponse<Group>> createGroup(@Body Group item);

    @PUT("groups/{id}")
    Flowable<HabitResponse<Void>> updateGroup(@Path("id") String id, @Body Group item);

    @GET("groups/{gid}/chat")
    Flowable<HabitResponse<List<ChatMessage>>> listGroupChat(@Path("gid") String groupId);

    @POST("groups/{gid}/join")
    Flowable<HabitResponse<Group>> joinGroup(@Path("gid") String groupId);

    @POST("groups/{gid}/leave")
    Flowable<HabitResponse<Void>> leaveGroup(@Path("gid") String groupId);

    @POST("groups/{gid}/chat")
    Flowable<HabitResponse<PostChatMessageResult>> postGroupChat(@Path("gid") String groupId, @Body Map<String, String> message);

    @DELETE("groups/{gid}/chat/{messageId}")
    Flowable<HabitResponse<Void>> deleteMessage(@Path("gid") String groupId, @Path("messageId") String messageId);

    @DELETE("inbox/messages/{messageId}")
    Flowable<HabitResponse<Void>> deleteInboxMessage(@Path("messageId") String messageId);

    @GET("groups/{gid}/members")
    Flowable<HabitResponse<List<Member>>> getGroupMembers(@Path("gid") String groupId, @Query("includeAllPublicFields") Boolean includeAllPublicFields);

    @GET("groups/{gid}/members")
    Flowable<HabitResponse<List<Member>>> getGroupMembers(@Path("gid") String groupId, @Query("includeAllPublicFields") Boolean includeAllPublicFields, @Query("lastId") String lastId);

    // Like returns the full chat list
    @POST("groups/{gid}/chat/{mid}/like")
    Flowable<HabitResponse<ChatMessage>> likeMessage(@Path("gid") String groupId, @Path("mid") String mid);

    @POST("groups/{gid}/chat/{mid}/flag")
    Flowable<HabitResponse<Void>> flagMessage(@Path("gid") String groupId, @Path("mid") String mid, @Body Map<String, String> data);

    @POST("groups/{gid}/chat/seen")
    Flowable<HabitResponse<Void>> seenMessages(@Path("gid") String groupId);

    @POST("groups/{gid}/invite")
    Flowable<HabitResponse<List<String>>> inviteToGroup(@Path("gid") String groupId, @Body Map<String, Object> inviteData);

    @POST("groups/{gid}/reject-invite")
    Flowable<HabitResponse<Void>> rejectGroupInvite(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/accept")
    Flowable<HabitResponse<Void>> acceptQuest(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/reject")
    Flowable<HabitResponse<Void>> rejectQuest(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/cancel")
    Flowable<HabitResponse<Void>> cancelQuest(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/force-start")
    Flowable<HabitResponse<Quest>> forceStartQuest(@Path("gid") String groupId, @Body Group group);

    @POST("groups/{gid}/quests/invite/{questKey}")
    Flowable<HabitResponse<Quest>> inviteToQuest(@Path("gid") String groupId, @Path("questKey") String questKey);

    @POST("groups/{gid}/quests/abort")
    Flowable<HabitResponse<Quest>> abortQuest(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/leave")
    Flowable<HabitResponse<Void>> leaveQuest(@Path("gid") String groupId);

    @POST("/iap/android/verify")
    Flowable<HabitResponse<PurchaseValidationResult>> validatePurchase(@Body PurchaseValidationRequest request);

    @POST("/iap/android/subscribe")
    Flowable<HabitResponse<Void>> validateSubscription(@Body SubscriptionValidationRequest request);
    @POST("/iap/android/norenew-subscribe")
    Flowable<HabitResponse<Void>> validateNoRenewSubscription(@Body PurchaseValidationRequest request);

    @POST("user/custom-day-start")
    Flowable<HabitResponse<User>> changeCustomDayStart(@Body Map<String, Object> updateObject);

    //Members URL
    @GET("members/{mid}")
    Flowable<HabitResponse<Member>> getMember(@Path("mid") String memberId);
    @GET("members/username/{username}")
    Flowable<HabitResponse<Member>> getMemberWithUsername(@Path("username") String username);

    @GET("members/{mid}/achievements")
    Flowable<HabitResponse<List<Achievement>>> getMemberAchievements(@Path("mid") String memberId);

    @POST("members/send-private-message")
    Flowable<HabitResponse<PostChatMessageResult>> postPrivateMessage(@Body Map<String, String> messageDetails);

    @GET("members/find/{username}")
    Flowable<HabitResponse<List<FindUsernameResult>>> findUsernames(@Path("username") String username, @Query("context") String context, @Query("id") String id);

    @GET("shops/{identifier}")
    Flowable<HabitResponse<Shop>> retrieveShopInventory(@Path("identifier") String identifier);
    @GET("shops/market-gear")
    Flowable<HabitResponse<Shop>> retrieveMarketGear();

    //Push notifications
    @POST("user/push-devices")
    Flowable<HabitResponse<List<Void>>> addPushDevice(@Body Map<String, String> pushDeviceData);

    @DELETE("user/push-devices/{regId}")
    Flowable<HabitResponse<List<Void>>> deletePushDevice(@Path("regId") String regId);

    /* challenges api */

    @GET("challenges/user")
    Flowable<HabitResponse<List<Challenge>>> getUserChallenges(@Query("page") Integer page, @Query("member") boolean memberOnly);
    @GET("challenges/user")
    Flowable<HabitResponse<List<Challenge>>> getUserChallenges(@Query("page") Integer page);

    @GET("tasks/challenge/{challengeId}")
    Flowable<HabitResponse<TaskList>> getChallengeTasks(@Path("challengeId") String challengeId);

    @GET("challenges/{challengeId}")
    Flowable<HabitResponse<Challenge>> getChallenge(@Path("challengeId") String challengeId);

    @POST("challenges/{challengeId}/join")
    Flowable<HabitResponse<Challenge>> joinChallenge(@Path("challengeId") String challengeId);

    @POST("challenges/{challengeId}/leave")
    Flowable<HabitResponse<Void>> leaveChallenge(@Path("challengeId") String challengeId, @Body LeaveChallengeBody body);

    @POST("challenges")
    Flowable<HabitResponse<Challenge>> createChallenge(@Body Challenge challenge);

    @POST("tasks/challenge/{challengeId}")
    Flowable<HabitResponse<List<Task>>> createChallengeTasks(@Path("challengeId") String challengeId, @Body List<Task> tasks);

    @POST("tasks/challenge/{challengeId}")
    Flowable<HabitResponse<Task>> createChallengeTask(@Path("challengeId") String challengeId, @Body Task task);

    @PUT("challenges/{challengeId}")
    Flowable<HabitResponse<Challenge>> updateChallenge(@Path("challengeId") String challengeId, @Body Challenge challenge);

    @DELETE("challenges/{challengeId}")
    Flowable<HabitResponse<Void>> deleteChallenge(@Path("challengeId") String challengeId);


    //DEBUG: These calls only work on a local development server

    @POST("debug/add-ten-gems")
    Flowable<HabitResponse<Void>> debugAddTenGems();

    // Notifications
    @POST("notifications/{notificationId}/read")
    Flowable<HabitResponse<List>> readNotification(@Path("notificationId") String notificationId);

    @POST("notifications/read")
    Flowable<HabitResponse<List>> readNotifications(@Body Map<String, List<String>> notificationIds);

    @POST("notifications/see")
    Flowable<HabitResponse<List>> seeNotifications(@Body Map<String, List<String>> notificationIds);

    @POST("user/open-mystery-item")
    Flowable<HabitResponse<Equipment>> openMysteryItem();

    @POST("cron")
    Flowable<HabitResponse<Void>> runCron();

    @POST("user/reset")
    Flowable<HabitResponse<Void>> resetAccount();

    @HTTP(method = "DELETE", path = "user", hasBody = true)
    Flowable<HabitResponse<Void>> deleteAccount(@Body Map<String, String> body);

    @GET("user/toggle-pinned-item/{pinType}/{path}")
    Flowable<HabitResponse<Void>> togglePinnedItem(@Path("pinType") String pinType,@Path("path") String path);

    @POST("user/reset-password")
    Flowable<HabitResponse<Void>> sendPasswordResetEmail(@Body Map<String, String> data);

    @PUT("user/auth/update-username")
    Flowable<HabitResponse<Void>> updateLoginName(@Body Map<String, String> data);

    @POST("user/auth/verify-username")
    Flowable<HabitResponse<VerifyUsernameResponse>> verifyUsername(@Body Map<String, String> data);

    @PUT("user/auth/update-email")
    Flowable<HabitResponse<Void>> updateEmail(@Body Map<String, String> data);

    @PUT("user/auth/update-password")
    Flowable<HabitResponse<Void>> updatePassword(@Body Map<String, String> data);

    @POST("user/allocate")
    Flowable<HabitResponse<Stats>> allocatePoint(@Query("stat") String stat);

    @POST("user/allocate-bulk")
    Flowable<HabitResponse<Stats>> bulkAllocatePoints(@Body Map<String, Map<String, Integer>> stats);

    @GET("world-state")
    Flowable<HabitResponse<WorldState>> getWorldState();
}
