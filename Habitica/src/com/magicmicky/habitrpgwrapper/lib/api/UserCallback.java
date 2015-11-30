package com.magicmicky.habitrpgwrapper.lib.api;

import android.util.Log;

import com.google.gson.Gson;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Simple implementation of a user callback.
 * Created by MagicMicky on 10/06/2014.
 */
public class UserCallback implements Callback<HabitRPGUser> {
    private static final int BUFFER_SIZE = 0x1000;
    private final String TAG = "HabitRPGDataCallback";

    public static void longInfo(String str) {
        if (str.length() > 4000) {
            System.out.println(str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            System.out.println(str);
    }

    @Override
    public void success(HabitRPGUser habitRPGUserCallback, Response response) {
        Log.d(TAG, "Success ! " + habitRPGUserCallback.getId());
        longInfo(new Gson().toJson(habitRPGUserCallback));
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.w(TAG, "Failure ! " + retrofitError.getUrl());
        Log.e(TAG, retrofitError.getMessage());
        Log.e(TAG, "Network?" + retrofitError.isNetworkError());
    }

}
