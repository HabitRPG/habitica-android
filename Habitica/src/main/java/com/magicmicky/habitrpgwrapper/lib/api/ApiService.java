package com.magicmicky.habitrpgwrapper.lib.api;

import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.ContentResult;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Items;
import com.magicmicky.habitrpgwrapper.lib.models.PostChatMessageResult;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationResult;
import com.magicmicky.habitrpgwrapper.lib.models.Quest;
import com.magicmicky.habitrpgwrapper.lib.models.Shop;
import com.magicmicky.habitrpgwrapper.lib.models.Status;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuth;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthSocial;
import com.magicmicky.habitrpgwrapper.lib.models.responses.BuyResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.FeedResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.SkillResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.UnlockResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
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
    Observable<Status> getStatus();

    @GET("content")
    Observable<ContentResult> getContent(@Query("language") String language);

    /* user API */

    @GET("user/")
    Observable<HabitRPGUser> getUser();

    @PUT("user/")
    Observable<HabitRPGUser> updateUser(@Body Map<String, Object> updateDictionary);

    @GET("user/inventory/buy")
    Observable<List<ItemData>> getInventoryBuyableGear();

    @POST("user/equip/{type}/{key}")
    Observable<Items> equipItem(@Path("type") String type, @Path("key") String itemKey);

    @POST("user/buy/{key}")
    Observable<BuyResponse> buyItem(@Path("key") String itemKey);

    @POST("user/purchase/{type}/{key}")
    Observable<Void> purchaseItem(@Path("type") String type, @Path("key") String itemKey);

    @POST("user/purchase-hourglass/{type}/{key}")
    Observable<Void> purchaseHourglassItem(@Path("type") String type, @Path("key") String itemKey);

    @POST("user/buy-mystery-set/{key}")
    Observable<Void> purchaseMysterySet(@Path("key") String itemKey);

    @POST("user/buy-quest/{key}")
    Observable<Void> purchaseQuest(@Path("key") String key);

    @POST("user/sell/{type}/{key}")
    Observable<HabitRPGUser> sellItem(@Path("type") String itemType, @Path("key") String itemKey);

    @POST("user/feed/{pet}/{food}")
    Observable<FeedResponse> feedPet(@Path("pet") String petKey, @Path("food") String foodKey);

    @POST("user/hatch/{egg}/{hatchingPotion}")
    Observable<Items> hatchPet(@Path("egg") String eggKey, @Path("hatchingPotion") String hatchingPotionKey);


    @GET("tasks/user")
    Observable<TaskList> getTasks();

    @POST("user/unlock")
    Observable<UnlockResponse> unlockPath(@Query("path") String path);

    @GET("tasks/{id}")
    Observable<Task> getTask(@Path("id") String id);

    @POST("tasks/{id}/score/{direction}")
    Observable<TaskDirectionData> postTaskDirection(@Path("id") String id, @Path("direction") String direction);

    @POST("tasks/{id}/move/to/{position}")
    Observable<Void> postTaskNewPosition(@Path("id") String id, @Path("position") String position);

    @POST("tasks/{taskId}/checklist/{itemId}/score")
    Observable<Task> scoreChecklistItem(@Path("taskId") String taskId, @Path("itemId") String itemId);

    @POST("tasks/user")
    Observable<Task> createItem(@Body Task item);

    @POST("tasks/user")
    Observable<List<Task>> createTasks(@Body List<Task> tasks);

    @PUT("tasks/{id}")
    Observable<Task> updateTask(@Path("id") String id, @Body Task item);

    @DELETE("tasks/{id}")
    Observable<Void> deleteTask(@Path("id") String id);


    @POST("tags")
    Observable<Tag> createTag(@Body Tag tag);

    @PUT("tags/{id}")
    Observable<Tag> updateTag(@Path("id") String id, @Body Tag tag);

    @DELETE("tags/{id}")
    Observable<Void> deleteTag(@Path("id") String id);


    @POST("user/auth/local/register")
    Observable<UserAuthResponse> registerUser(@Body UserAuth auth);

    @POST("user/auth/local/login")
    Observable<UserAuthResponse> connectLocal(@Body UserAuth auth);

    @POST("user/auth/social")
    Observable<UserAuthResponse> connectSocial(@Body UserAuthSocial auth);


    @POST("user/sleep")
    Observable<Void> sleep();

    @POST("user/revive")
    Observable<HabitRPGUser> revive();

    @POST("user/class/cast/{skill}")
    Observable<SkillResponse> useSkill(@Path("skill") String skillName, @Query("targetType") String targetType, @Query("targetId") String targetId);

    @POST("user/class/cast/{skill}")
    Observable<SkillResponse> useSkill(@Path("skill") String skillName, @Query("targetType") String targetType);

    @POST("user/change-class")
    Observable<HabitRPGUser> changeClass();

    @POST("user/change-class")
    Observable<HabitRPGUser> changeClass(@Query("class") String className);

    @POST("user/disable-classes")
    Observable<HabitRPGUser> disableClasses();

    @POST("user/mark-pms-read")
    Observable<Void> markPrivateMessagesRead();

    /* Group API */

    @GET("groups")
    Observable<List<Group>> listGroups(@Query("type") String type);

    @GET("groups/{gid}")
    Observable<Group> getGroup(@Path("gid") String groupId);

    @PUT("groups/{id}")
    Observable<Void> updateGroup(@Path("id") String id, @Body Group item);

    @GET("groups/{gid}/chat")
    Observable<List<ChatMessage>> listGroupChat(@Path("gid") String groupId);

    @POST("groups/{gid}/join")
    Observable<Group> joinGroup(@Path("gid") String groupId);

    @POST("groups/{gid}/leave")
    Observable<Void> leaveGroup(@Path("gid") String groupId);

    @POST("groups/{gid}/chat")
    Observable<PostChatMessageResult> postGroupChat(@Path("gid") String groupId, @Body HashMap<String, String> message);

    @DELETE("groups/{gid}/chat/{messageId}")
    Observable<Void> deleteMessage(@Path("gid") String groupId, @Path("messageId") String messageId);

    @GET("groups/{gid}/members")
    Observable<List<HabitRPGUser>> getGroupMembers(@Path("gid") String groupId, @Query("includeAllPublicFields") Boolean includeAllPublicFields);

    @GET("groups/{gid}/members")
    Observable<List<HabitRPGUser>> getGroupMembers(@Path("gid") String groupId, @Query("includeAllPublicFields") Boolean includeAllPublicFields, @Query("lastId") String lastId);

    // Like returns the full chat list
    @POST("groups/{gid}/chat/{mid}/like")
    Observable<ChatMessage> likeMessage(@Path("gid") String groupId, @Path("mid") String mid);

    @POST("groups/{gid}/chat/{mid}/flag")
    Observable<Void> flagMessage(@Path("gid") String groupId, @Path("mid") String mid);

    @POST("groups/{gid}/chat/seen")
    Observable<Void> seenMessages(@Path("gid") String groupId);

    @POST("groups/{gid}/invite")
    Observable<Void> inviteToGroup(@Path("gid") String groupId, @Body Map<String, Object> inviteData);

    @POST("groups/{gid}/reject-invite")
    Observable<Void> rejectGroupInvite(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/accept")
    Observable<Void> acceptQuest(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/reject")
    Observable<Void> rejectQuest(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/cancel")
    Observable<Void> cancelQuest(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/force-start")
    Observable<Quest> forceStartQuest(@Path("gid") String groupId, @Body Group group);

    @POST("groups/{gid}/quests/invite/{questKey}")
    Observable<Quest> inviteToQuest(@Path("gid") String groupId, @Path("questKey") String questKey);

    @POST("groups/{gid}/quests/abort")
    Observable<Quest> abortQuest(@Path("gid") String groupId);

    @POST("groups/{gid}/quests/leave")
    Observable<Void> leaveQuest(@Path("gid") String groupId);

    @POST("/iap/android/verify")
    Observable<PurchaseValidationResult> validatePurchase(@Body PurchaseValidationRequest request);

    //Members URL
    @GET("members/{mid}")
    Observable<HabitRPGUser> GetMember(@Path("mid") String memberId);

    @POST("members/send-private-message")
    Observable<PostChatMessageResult> postPrivateMessage(@Body HashMap<String, String> messageDetails);
    
    @GET("shops/{identifier}")
    Observable<Shop> fetchShopInventory(@Path("identifier") String identifier);

    //Push notifications
    @POST("user/push-devices")
    Observable<Void> addPushDevice(@Body Map<String, String> pushDeviceData);

    @DELETE("user/push-devices/{regId}")
    Observable<Void> deletePushDevice(@Path("regId") String regId);

    //DEBUG: These calls only work on a local development server

    @POST("debug/add-ten-gems")
    Observable<Void> debugAddTenGems();

}
