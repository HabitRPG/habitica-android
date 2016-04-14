package com.magicmicky.habitrpgwrapper.lib.api;

import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.ContentResult;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Items;
import com.magicmicky.habitrpgwrapper.lib.models.PostChatMessageResult;
import com.magicmicky.habitrpgwrapper.lib.models.Status;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuth;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthSocial;
import com.magicmicky.habitrpgwrapper.lib.models.responses.BuyResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.FeedResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.UnlockResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by MagicMicky on 10/06/2014.
 */
public interface ApiService {
    @GET("/status")
    void getStatus(Callback<Status> statusCallback);

    @GET("/content")
    void getContent(Callback<ContentResult> contentResultCallback);

    /* user API */

    @GET("/user/")
    void getUser(Callback<HabitRPGUser> habitRPGUserCallback);

    @PUT("/user/")
    void updateUser(@Body Map<String, Object> updateDictionary, Callback<HabitRPGUser> habitRPGUserCallback);

    @GET("/user/inventory/buy")
    void getInventoryBuyableGear(Callback<List<ItemData>> buyableGearCallback);

    @POST("/user/inventory/equip/{type}/{key}")
    void equipItem(@Path("type") String type, @Path("key") String itemKey, Callback<Items> gearCallback);

    @POST("/user/inventory/buy/{key}")
    void buyItem(@Path("key") String itemKey, Callback<BuyResponse> voidCallback);

    @POST("/user/inventory/sell/{type}/{key}")
    void sellItem(@Path("type") String itemType, @Path("key") String itemKey, Callback<HabitRPGUser> voidCallback);

    @POST("/user/inventory/feed/{pet}/{food}")
    void feedPet(@Path("pet") String petKey, @Path("food") String foodKey, Callback<FeedResponse> feedingCallback);

    @POST("/user/inventory/hatch/{egg}/{hatchingPotion}")
    void hatchPet(@Path("egg") String eggKey, @Path("hatchingPotion") String hatchingPotionKey, Callback<Items> itemsCallback);


    @POST("/user/unlock")
    void unlockPath(@Query("path") String path, Callback<UnlockResponse> unlockResponseCallback);

    @GET("/user/tasks/{id}")
    void getTask(@Path("id") String id, Callback<Task> habitItemCallback);

    @POST("/user/tasks/{id}/{direction}")
    void postTaskDirection(@Path("id") String id, @Path("direction") String direction, Callback<TaskDirectionData> taskDirectionCallback);

    @POST("/user/tasks")
    void createItem(@Body Task item, Callback<Task> habitItemCallback);


    @PUT("/user/tasks/{id}")
    void updateTask(@Path("id") String id, @Body Task item, Callback<Task> habitItemCallback);


    @DELETE("/user/tasks/{id}")
    void deleteTask(@Path("id") String id, Callback<Void> voidCallback);


    @POST("/user/tags")
    void createTag(@Body Tag tag, Callback<List<Tag>> multiTagCallback);


    @PUT("/user/tags/{id}")
    void updateTag(@Path("id") String id, @Body Tag tag, Callback<Tag> multiTagCallback);


    @DELETE("/user/tags/{id}")
    void deleteTag(@Path("id") String id, Callback<Void> voidCallback);

    @POST("/register")
    void registerUser(@Body UserAuth auth, Callback<UserAuthResponse> callback);

    @POST("/user/auth/local")
    void connectLocal(@Body UserAuth auth, Callback<UserAuthResponse> callback);

    @POST("/user/auth/social")
    void connectSocial(@Body UserAuthSocial auth, Callback<UserAuthResponse> callback);

    @POST("/user/sleep")
    void sleep(Callback<Void> voidCallback);

    @POST("/user/revive")
    void revive(Callback<HabitRPGUser> habitRPGUserCallback);

    @POST("/user/class/cast/{skill}")
    void useSkill(@Path("skill") String skillName, @Query("targetType") String targetType, @Query("targetId") String targetId, Callback<HabitRPGUser> habitRPGUserCallback);

    @POST("/user/class/cast/{skill}")
    void useSkill(@Path("skill") String skillName, @Query("targetType") String targetType, Callback<HabitRPGUser> habitRPGUserCallback);

    @POST("/user/class/change")
    void changeClass(@Query("class") String className, Callback<HabitRPGUser> cb);

    /* Group API */

    @GET("/groups")
    void listGroups(@Query("type") String type, Callback<ArrayList<Group>> cb);

    @GET("/groups/{gid}")
    void getGroup(@Path("gid") String groupId, Callback<Group> cb);

    @POST("/groups/{id}")
    void updateGroup(@Path("id") String id, @Body Group item, Callback<Void> habitItemCallback);

    @GET("/groups/{gid}/chat")
    void listGroupChat(@Path("gid") String groupId, Callback<List<ChatMessage>> cb);

    @POST("/groups/{gid}/join")
    void joinGroup(@Path("gid") String groupId, Callback<Group> cb);

    @POST("/groups/{gid}/leave")
    void leaveGroup(@Path("gid") String groupId, Callback<Group> cb);

    @POST("/groups/{gid}/chat")
    void postGroupChat(@Path("gid") String groupId, @Query("message") String message, Callback<PostChatMessageResult> cb);

    @DELETE("/groups/{gid}/chat/{messageId}")
    void deleteMessage(@Path("gid") String groupId, @Path("messageId") String messageId, Callback<Void> cb);

    // Like returns the full chat list
    @POST("/groups/{gid}/chat/{mid}/like")
    void likeMessage(@Path("gid") String groupId, @Path("mid") String mid, Callback<List<Void>> cb);

    @POST("/groups/{gid}/chat/{mid}/flag")
    void flagMessage(@Path("gid") String groupId, @Path("mid") String mid, Callback<Void> cb);

    @POST("/groups/{gid}/chat/seen")
    void seenMessages(@Path("gid") String groupId, Callback<String> cb);

    @POST("/user/batch-update")
    void batchOperation(@Body List<Map<String,Object>> operations, Callback<HabitRPGUser> cb);

    @POST("/groups/{gid}/questAccept")
    void acceptQuest(@Path("gid") String groupId, Callback<Void> cb);

    @POST("/groups/{gid}/questReject")
    void rejectQuest(@Path("gid") String groupId, Callback<Void> cb);

    @POST("/groups/{gid}/questCancel")
    void cancelQuest(@Path("gid") String groupId, Callback<Void> cb);

    @POST("/groups/{gid}/questAccept?force=true")
    void forceStartQuest(@Path("gid") String groupId, @Body Group group, Callback<Group> cb);

    @POST("/groups/{gid}/questAccept")
    void inviteToQuest(@Path("gid") String groupId, @Query("key") String questKey, Callback<Group> cb);
    @POST("/groups/{gid}/questAbort")
    void abortQuest(@Path("gid") String groupId, Callback<Group> cb);

    @POST("/groups/{gid}/questLeave")
    void leaveQuest(@Path("gid") String groupId, Callback<Void> cb);
}
