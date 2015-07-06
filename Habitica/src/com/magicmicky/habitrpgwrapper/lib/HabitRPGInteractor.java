package com.magicmicky.habitrpgwrapper.lib;

import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
import com.magicmicky.habitrpgwrapper.lib.api.Server;
import com.magicmicky.habitrpgwrapper.lib.api.TypeAdapter.TagsAdapter;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Status;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuth;
import com.magicmicky.habitrpgwrapper.lib.models.UserAuthResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Habit;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Reward;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Tags;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ToDo;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by MagicMicky on 13/06/2014.
 */
public class HabitRPGInteractor {

    private ApiService apiService;
    public HabitRPGInteractor(final String apiKey, final String userKey, final Server server) {
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader("x-api-key", apiKey);
                request.addHeader("x-api-user",userKey);
            }
        };

        //Exclusion stratety needed for DBFlow https://github.com/Raizlabs/DBFlow/issues/121
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaredClass().equals(ModelAdapter.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                }).registerTypeAdapter(Tags.class, new TagsAdapter().nullSafe()).create();

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(server.toString())
                .setRequestInterceptor(requestInterceptor)
                .setConverter(new GsonConverter(gson))
                .build();
        this.apiService  = adapter.create(ApiService.class);
    }
    public HabitRPGInteractor(final String apiKey, final String userKey) {
        this(apiKey, userKey, Server.NORMAL);
    }
    /**
     * Retrieve the Status of habitrpg
     * @see com.magicmicky.habitrpgwrapper.lib.models.Status
     * @param statusCallback the callback called when status is retrieved
     */
    public void getStatus(Callback<Status> statusCallback) {
        this.apiService.getStatus(statusCallback);
    }

    /**
     * Retrieve a User from HabitRPG's API.
     * @see com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser
     * @param callback  The callback called when the user is retrieved
     */
    public void getUser(Callback<HabitRPGUser> callback) {
        this.apiService.getUser(callback);
    }

    /**
     * Retrieve a daily from HabitRPG's API
     * @param dailyId       the id of the daily to retrieve
     * @param dailyCallback the callback called when the daily is retrieved
     * @see com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily
     */
    public void getDaily(String dailyId, Callback<Daily> dailyCallback) {
        this.apiService.getDaily(dailyId,dailyCallback);
    }

    /**
     * Retrieve a Habit from HabitRPG's API
     * @param habitId       the id of the habit to retrieve.
     * @param habitCallback the callback called when the habit is retrieved.
     * @see com.magicmicky.habitrpgwrapper.lib.models.tasks.Habit
     */
    public void getHabit(String habitId, Callback<Habit> habitCallback) {
        this.apiService.getHabit(habitId,habitCallback);
    }

    /**
     * Retrieve a To do from HabitRPG's API
     * @param todoId        the id of the item to retrieve
     * @param todoCallback  the callback called when the item is retrieved.
     * @see com.magicmicky.habitrpgwrapper.lib.models.tasks.ToDo
     */
    public void getToDo(String todoId, Callback<ToDo> todoCallback) {
        this.apiService.getToDo(todoId,todoCallback);
    }

    /**
     * Retrieve a Reward form HabitRPG's API
     * @param rewardId          the id of the reward to retrieve
     * @param rewardCallback    the callback called when the reward is retrieved.
     * @see com.magicmicky.habitrpgwrapper.lib.models.tasks.Reward
     */
    public void getReward(String rewardId, Callback<Reward> rewardCallback) {
        this.apiService.getReward(rewardId,rewardCallback);
    }

    /**
     * Update the task to "up" or "down", and check or uncheck dailies/todos.
     * @param taskId                the id of the task to update
     * @param direction             the direction of the task
     * @param taskDirectionCallback the callback called when the direction is set.
     * @see com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData
     */
    public void postTaskDirection(String taskId, TaskDirection direction, Callback<TaskDirectionData> taskDirectionCallback) {
        this.apiService.postTaskDirection(taskId, direction.toString(), taskDirectionCallback);
    }

    /**
     * Create a daily on HabitRPG
     * @param daily         the daily to create
     * @param dailyCallback the callback called when the daily is created
     * @see com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily
     */
    public void createItem(Daily daily, Callback<Daily> dailyCallback) {
            this.apiService.createItem(daily, dailyCallback );
    }

    /**
     * Create an Habit
     * @param habit         the haibt to create
     * @param habitCallback the callback called once the habit is created
     * @see com.magicmicky.habitrpgwrapper.lib.models.tasks.Habit
     */
    public void createItem(Habit habit, Callback<Habit> habitCallback) {
        this.apiService.createItem(habit, habitCallback);
    }

    /**
     * Create a To do
     * @param todoItem          the item to create
     * @param toDoCallback  the callback called once the item is created
     * @see com.magicmicky.habitrpgwrapper.lib.models.tasks.ToDo
     */
    public void createItem(ToDo todoItem, Callback<ToDo> toDoCallback) {
        this.apiService.createItem(todoItem, toDoCallback);
    }

    /**
     * Creates a reward
     * @param reward            the reward to create
     * @param rewardCallback    the callback called once the item is created
     */
    public void createItem(Reward reward, Callback<Reward> rewardCallback) {
        this.apiService.createItem(reward, rewardCallback);
    }

    /**
     * Update an habit
     * @param habitId       the id of the habit to update
     * @param habit         the habit to update, with updated field
     * @param habitCallback the callback called once the habit is updated
     */
    public void updateItem(String habitId, Habit habit, Callback<Habit> habitCallback) {
        this.apiService.updateTask(habitId, habit, habitCallback);
    }

    /**
     * Updates a daily
     * @param dailyId       the id of the daily to update
     * @param daily         the new daily item, with updated field
     * @param habitCallback the callback called once the daily is updated
     */
    public void updateItem(String dailyId, Daily daily, Callback<Daily> habitCallback) {
        this.apiService.updateTask(dailyId, daily, habitCallback);
    }

    /**
     * Updates a To do item
     * @param todoId        the id of the item to update
     * @param todoItem      the item to udpate, with updated field
     * @param toDoCallback  the callback called once the item is updated
     */
    public void updateItem(String todoId, ToDo todoItem, Callback<ToDo> toDoCallback) {
        this.apiService.updateTask(todoId, todoItem, toDoCallback);
    }

    /**
     * Updates a Reward
     * @param rewardId          the id of the reward to update
     * @param reward            the reward to update, with updated field
     * @param rewardCallback    the callback called once the item is updated
     */
    public void updateItem(String rewardId, Reward reward, Callback<Reward> rewardCallback) {
        this.apiService.updateTask(rewardId, reward, rewardCallback);
    }

    /**
     * Deletes a task.
     * @param itemId        the id of the task to delete
     * @param voidCallback  the callback (on void) called once the item is deleted
     */
    public void deleteItem(String itemId, Callback<Void> voidCallback) {
        this.apiService.deleteTask(itemId, voidCallback);
    }

    /**
     * Creates a tag
     * @param tag               The tag to create
     * @param multiTagCallback  the callback called once the tag is created
     */
    public void createTag(Tag tag, Callback<List<Tag>> multiTagCallback) {
        this.apiService.createTag(tag, multiTagCallback);
    }

    /**
     * Updates a tag
     * @param tagId         The id of the tag to udpate
     * @param tag           The tag to update, with updated field
     * @param tagCallback   The callback called once the tag is updated
     */
    public void updateTag(String tagId, Tag tag, Callback<Tag> tagCallback) {
        this.apiService.updateTag(tagId, tag, tagCallback);
    }

    /**
     * Deletes a tag
     * @param tagId         the id of the tag to delete
     * @param voidCallback  the callback (on void) called once the item is deleted
     */
    public void deleteTag(String tagId, Callback<Void> voidCallback) {
        this.apiService.deleteTag(tagId, voidCallback);
    }

    /**
     * Connects a user
     * @param authData          The username & password of the user
     * @param responseCallback  The callback called once the user is connected
     */
    public void connectUser(UserAuth authData, Callback<UserAuthResponse> responseCallback) {
        this.apiService.connectLocal(authData,responseCallback);
    }

}
