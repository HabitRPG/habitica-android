package com.magicmicky.habitrpgmobileapp.callbacks;

import android.util.Log;

import com.magicmicky.habitrpgwrapper.lib.HabitRPGInteractor;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by magicmicky on 18/02/15.
 */
public class HabitRPGUserCallback implements Callback<HabitRPGUser> {

    private final OnUserReceived mCallback;

    public HabitRPGUserCallback(OnUserReceived callback) {
        this.mCallback =  callback;
    }
    @Override
    public void success(HabitRPGUser habitRPGUser, Response response) {
        Log.d("OMG", "user retrieved!");
        mCallback.onUserReceived(habitRPGUser);
    }

    @Override
    public void failure(RetrofitError error) {
        Log.w("OMG", "user failed!" + error.getMessage());

        mCallback.onUserFail();
    }

    public interface OnUserReceived {
        public void onUserReceived(HabitRPGUser user);
        public void onUserFail();
    }
}
