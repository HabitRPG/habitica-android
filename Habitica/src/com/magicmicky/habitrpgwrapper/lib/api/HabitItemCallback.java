package com.magicmicky.habitrpgwrapper.lib.api;

import android.util.Log;

import com.google.gson.Gson;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Checklist;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Habit;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Reward;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ToDo;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * TODO: Tags
 * Simple implementation of a HabitItem callback, supposed to be called when the result is either a single
 * to do, daily, habit or reward.
 * Created by MagicMicky on 10/06/2014.
 */
public class HabitItemCallback<T extends HabitItem> implements Callback<T> {
    private static final String TAG = "HabitCallback";

    @Override
    public void success(T habitItem, Response response) {
        if(habitItem instanceof ToDo) {
            Log.d(TAG, "todo");

        } else if(habitItem instanceof Habit){
            Log.d(TAG, "habit");
        } else if(habitItem instanceof Daily) {
            Log.d(TAG, "daily");
            ((Daily) habitItem).addItem(new Checklist.ChecklistItem("OMG"));
        } else if(habitItem instanceof Reward) {
            Log.d(TAG, "reward");
        } else {
            Log.d(TAG, "Unknown");
        }

        Log.d(TAG +"_ans", new Gson().toJson(habitItem));

    }

    @Override
    public void failure(RetrofitError retrofitError){
        Log.w(TAG, "Failure ! ");
        Log.e(TAG, retrofitError.getUrl() + ":" + retrofitError.getMessage());
        Log.e(TAG, "Network?" + retrofitError.isNetworkError());
        Log.e(TAG, "HTTP Response:" + retrofitError.getResponse().getStatus() + " - " + retrofitError.getResponse().getReason());
    }
}
