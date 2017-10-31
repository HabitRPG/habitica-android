package com.habitrpg.android.habitica.api;

import com.habitrpg.android.habitica.models.AchievementResult;
import com.habitrpg.android.habitica.models.ContentResult;
import com.habitrpg.android.habitica.models.LeaveChallengeBody;
import com.habitrpg.android.habitica.models.PurchaseValidationRequest;
import com.habitrpg.android.habitica.models.PurchaseValidationResult;
import com.habitrpg.android.habitica.models.SubscriptionValidationRequest;
import com.habitrpg.android.habitica.models.Tag;
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
import com.habitrpg.android.habitica.models.shops.Shop;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.social.Group;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.user.Items;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;
import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;


/**
 * Created by MagicMicky on 10/06/2014.
 */
public interface ApiService {
    @GET("status")
    Observable<HabitResponse<Status>> getStatus();

    @GET("content")
    Observable<HabitResponse<ContentResult>> getContent(@Query("language") String language);

    /* user API */

    @GET("user/")
    Observable<HabitResponse<User>> getUser();

    @PUT("user/")
    Observable<HabitResponse<User>> updateUser(@Body Map<String, Object> updateDictionary);

    @PUT("user/")
    Observable<HabitResponse<User>> registrationLanguage(@Header("Accept-Language") String registrationLanguage);

    @GET("user/in-app-rewards")
    Observable<HabitResponse<List<ShopItem>>> retrieveInAppRewards();
    @GET("user/inventory/buy")
    Observable<HabitResponse<List<ShopItem>>> retrieveOldGearRewards();

    @POST("user/equip/{type}/{key}")
    Observable<HabitResponse<Items>> equipItem(@Path("type") String type, @Path("key") String itemKey);

    @POST("user/buy/{key}")
    Observable<HabitResponse<BuyResponse>> buyItem(@Path("key") String itemKey);

    @POST("user/purchase/{type}/{key}")
    Observable<HabitResponse<Void>> purchaseItem(@Path("type") String type, @Path("key") String itemKey);

    @POST("user/purchase-hourglass/{type}/{key}")
    Observable<HabitResponse<Void>> purchaseHourglassItem(@Path("type") String type, @Path("key") String itemKey);

    @POST("user/buy-mystery-set/{key}")
    Observable<HabitResponse<Void>> purchaseMysterySet(@Path("key") String itemKey);

    @POST("user/buy-quest/{key}")
    Observable<HabitResponse<Void>> purchaseQuest(@Path("key") String key);

    @POST("user/sell/{type}/{key}")
    Observable<HabitResponse<User>> sellItem(@Path("type") String itemType, @Path("key") String itemKey);

    @POST("user/feed/{pet}/{food}")
    Observable<HabitResponse<FeedResponse>> feedPet(@Path("pet") String petKey, @Path("food") String foodKey);

    @POST("user/hatch/{egg}/{hatchingPotion}")
    Observable<HabitResponse<Items>> hatchPet(@Path("egg") String eggKey, @Path("hatchingPotion") String hatchingPotionKey);


    @GET("tasks/user")
    Observable<HabitResponse<TaskList>> getTasks();

    @GET("tasks/user")
    Observable<HabitResponse<TaskList>> getTasks(@Query("type") String type);

    @GET("tasks/user")
    Observable<HabitResponse<TaskList>> getTasks(@Query("type") String type, @Query("dueDate") String dueDate);


    @POST("user/unlock")
    Observable<HabitResponse<UnlockResponse>> unlockPath(@Query("path") String path);

    @GET("tasks/{id}")
    Observable<HabitResponse<Task>> getTask(@Path("id") String id);

    @POST("tasks/{id}/score/{direction}")
    Observable<HabitResponse<TaskDirectionData>> postTaskDirection(@Path("id") String id, @Path("direction") String direction);

    @POST("tasks/{id}/move/to/{position}")
    Observable<HabitResponse<List<String>>> postTaskNewPosition(@Path("id") String id, @Path("position") int position);

    @POST("tasks/{taskId}/checklist/{itemId}/score")
    Observable<HabitResponse<Task>> scoreChecklistItem(@Path("taskId") String taskId, @Path("itemId") String itemId);

    @POST("tasks/user")
    Observable<HabitResponse<Task>> createTask(@Body Task item);

    @POST("tasks/user")
    Observable<HabitResponse<List<Task>>> createTasks(@Body List<Task> tasks);

    @PUT("tasks/{id}")
    Observable<HabitResponse<Task>> updateTask(@Path("id") String id, @Body Task item);

    @DELETE("tasks/{id}")
    Observable<HabitResponse<Void>> deleteTask(@Path("id") String id);


    @POST("tags")
    Observable<HabitResponse<Tag>> createTag(@Body Tag tag);

    @PUT("tags/{id}")
    Observable<HabitResponse<Tag>> updateTag(@Path("id") String id, @Body Tag tag);

    @DELETE("tags/{id}")
    Observable<HabitResponse<Void>> deleteTag(@Path("id") String id);

    @POST("user/auth/local/register")
    Observable<HabitResponse<UserAuthResponse>> registerUser(@Body UserAuth auth);

    @POST("user/auth/local/login")
    Observable<HabitResponse<UserAuthResponse>> connectLocal(@Body UserAuth auth);

    @POST("user/auth/social")
    Observable<HabitResponse<UserAuthResponse>> connectSocial(@Body UserAuthSocial auth);


    @POST("user/sleep")
    Observable<HabitResponse<Boolean>> sleep();

    @POST("user/revive")
    Observable<HabitResponse<User>> revive();

    @POST("user/class/cast/{skill}")
    Observable<HabitResponse<SkillResponse>> useSkill(@Path("skill") String skillName, @Query("targetType") String targetType, @Query("targetId") String targetId);

    @POST("user/class/cast/{skill}")
    Observable<HabitResponse<SkillResponse>> useSkill(@Path("skill") String skillName, @Query("targetType") String targetType);

    @POST("user/change-class")
    Observable<HabitResponse<User>> changeClass();

    @POST("user/change-class")
    Observable<HabitResponse<User>> changeClass(@Query("class") String className);

    @POST("user/disable-classes")
    Observable<HabitResponse<User>> disableClasses();

    @POST("user/mark-pms-read")
    Observable<Void> markPrivateMessagesRead();



    /* Group API */

    @GET("groups")
    Observable<HabitResponse<List<Group>>> listGroups(@Query("type") String type);

    @GET("groups/{gid}")
    Observable<HabitResponse<Group>> getGroup(@Path("gid") String groupId);

    @PUT("groups/{id}")
    Observable<HabitResponse<Void>> updateGroup(@Path("id") String id, @Body Group item);

    @GET("groups/{gid}/chat")
    Observable<HabitResponse<List<ChatMessage>>> listGroupChat(@Path("gid") String groupId);

    @POST("groups/{gid}/join")
    Observable<HabitResponse<Group>> joinGroup(@Path("gid") String groupId);

    @POST("groups/{gid}/leave")
    Observable<HabitResponse<Void>> leaveGroup(@Path("gid") String groupId);

    @POST("groups/{gid}/chat")
    Observable<HabitResponse<PostChatMessageResult>> postGroupChat(@Path("gid") String groupId, @Body Map<String, String> message);

    @DELETE("groups/{gid}/chat/{messageId}")
    Observable<HabitResponse<Void>> deleteMessage(@Path("gid") String groupId, @Path("messageId") String messageId);

    @GET("groups/{gid}/members")
    Observable<HabitResponse<List<Member>>> getGroupMembers(@Path("gid") String groupId, @Query("includeAllPublicFields") Boolean includeAllPublicFields);

    @GET("groups/{gid}/members")
    Observable<HabitResponse<List<Member>>> getGroupMembers(@Path("gid") String groupId, @Query("includeAllPublicFields") Boolean includeAllPublicFields, @Query("lastId") String lastId);

    // Like returns the full chat list
    @POST("groups/{gid}/chat/{mid}/like")
    Observable<HabitResponse<ChatMessage>> likeMessage(@Path("gid") String groupId, @Path("mid") String mid);

    @POST("groups/{gid}/chat/{mid}/flag")
    Observable<HabitResponse<Void>> flagMessage(@Path("gid") String groupId, @Path("mid") String mid);

    @POST("groups/{gid}/chat/seen")
    Observable<HabitResponse<Void>> seenMessages(@Path("gid") String groupId);

    @POST("groups/{gid}/invite")
    Observable<HabitResponse<List<Void>>> inviteToGroup(@Path("gid") String groupId, @Body Map<String, Object> inviteData);

    @POST("groups/{gid}/reject-invite")
    Observable<HabitResponse<Void>> rejectGroupInvite(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/accept")
    Observable<HabitResponse<Void>> acceptQuest(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/reject")
    Observable<HabitResponse<Void>> rejectQuest(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/cancel")
    Observable<HabitResponse<Void>> cancelQuest(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/force-start")
    Observable<HabitResponse<Quest>> forceStartQuest(@Path("gid") String groupId, @Body Group group);

    @POST("groups/{gid}/quests/invite/{questKey}")
    Observable<HabitResponse<Quest>> inviteToQuest(@Path("gid") String groupId, @Path("questKey") String questKey);

    @POST("groups/{gid}/quests/abort")
    Observable<HabitResponse<Quest>> abortQuest(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/leave")
    Observable<HabitResponse<Void>> leaveQuest(@Path("gid") String groupId);

    @POST("/iap/android/verify")
    Observable<HabitResponse<PurchaseValidationResult>> validatePurchase(@Body PurchaseValidationRequest request);

    @POST("/iap/android/subscribe")
    Observable<HabitResponse<Void>> validateSubscription(@Body SubscriptionValidationRequest request);

    @POST("user/custom-day-start")
    Observable<HabitResponse<User>> changeCustomDayStart(@Body Map<String, Object> updateObject);

    //Members URL
    @GET("members/{mid}")
    Observable<HabitResponse<Member>> getMember(@Path("mid") String memberId);

    @GET("members/{mid}/achievements")
    Observable<HabitResponse<AchievementResult>> getMemberAchievements(@Path("mid") String memberId);

    @POST("members/send-private-message")
    Observable<HabitResponse<PostChatMessageResult>> postPrivateMessage(@Body Map<String, String> messageDetails);

    @GET("shops/{identifier}")
    Observable<HabitResponse<Shop>> retrieveShopInventory(@Path("identifier") String identifier);
    @GET("shops/market-gear")
    Observable<HabitResponse<Shop>> retrieveMarketGear();

    //Push notifications
    @POST("user/push-devices")
    Observable<HabitResponse<List<Void>>> addPushDevice(@Body Map<String, String> pushDeviceData);

    @DELETE("user/push-devices/{regId}")
    Observable<HabitResponse<List<Void>>> deletePushDevice(@Path("regId") String regId);

    /* challenges api */

    @GET("challenges/user")
    Observable<HabitResponse<List<Challenge>>> getUserChallenges();

    @GET("tasks/challenge/{challengeId}")
    Observable<HabitResponse<TaskList>> getChallengeTasks(@Path("challengeId") String challengeId);

    @GET("challenges/{challengeId}")
    Observable<HabitResponse<Challenge>> getChallenge(@Path("challengeId") String challengeId);

    @POST("challenges/{challengeId}/join")
    Observable<HabitResponse<Challenge>> joinChallenge(@Path("challengeId") String challengeId);

    @POST("challenges/{challengeId}/leave")
    Observable<HabitResponse<Void>> leaveChallenge(@Path("challengeId") String challengeId, @Body LeaveChallengeBody body);

    @POST("challenges")
    Observable<HabitResponse<Challenge>> createChallenge(@Body Challenge challenge);

    @POST("tasks/challenge/{challengeId}")
    Observable<HabitResponse<List<Task>>> createChallengeTasks(@Path("challengeId") String challengeId, @Body List<Task> tasks);

    @PUT("challenges/{challengeId}")
    Observable<HabitResponse<Challenge>> updateChallenge(@Path("challengeId") String challengeId, @Body Challenge challenge);

    @DELETE("challenges/{challengeId}")
    Observable<HabitResponse<Void>> deleteChallenge(@Path("challengeId") String challengeId);


    //DEBUG: These calls only work on a local development server

    @POST("debug/add-ten-gems")
    Observable<HabitResponse<Void>> debugAddTenGems();

    // Notifications
    @POST("notifications/{notificationId}/read")
    Observable<HabitResponse<List>> readNotification(@Path("notificationId") String notificationId);

    @POST("user/open-mystery-item")
    Observable<HabitResponse<Equipment>> openMysteryItem();

    @POST("cron")
    Observable<HabitResponse<Void>> runCron();

    @POST("user/reset")
    Observable<HabitResponse<Void>> resetAccount();

    @HTTP(method = "DELETE", path = "user", hasBody = true)
    Observable<HabitResponse<Void>> deleteAccount(@Body Map<String, String> body);

    @GET("user/toggle-pinned-item/{pinType}/{path}")
    Observable<HabitResponse<Void>> togglePinnedItem(@Path("pinType") String pinType,@Path("path") String path);

    @POST("user/reset-password")
    Observable<HabitResponse<Void>> sendPasswordResetEmail(@Body Map<String, String> data);

    @PUT("user/auth/update-username")
    Observable<HabitResponse<Void>> updateLoginName(@Body Map<String, String> data);

    @PUT("user/auth/update-email")
    Observable<HabitResponse<Void>> updateEmail(@Body Map<String, String> data);

    @PUT("user/auth/update-password")
    Observable<HabitResponse<Void>> updatePassword(@Body Map<String, String> data);

    @POST("user/allocate")
    Observable<HabitResponse<Stats>> allocatePoint(@Query("stat") String stat);

    @POST("user/allocate-bulk")
    Observable<HabitResponse<Stats>> bulkAllocatePoints(@Body Map<String, Map<String, Integer>> stats);
}
