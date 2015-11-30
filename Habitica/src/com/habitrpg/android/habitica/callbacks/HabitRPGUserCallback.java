package com.habitrpg.android.habitica.callbacks;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
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
        this.mCallback = callback;
    }

    @Override
    public void success(HabitRPGUser habitRPGUser, Response response) {
        habitRPGUser.async().save();


        mCallback.onUserReceived(habitRPGUser);
    }

    @Override
    public void failure(RetrofitError error) {
        Crashlytics.getInstance().core.logException(error);

        mCallback.onUserFail();
    }

    public interface OnUserReceived {
        void onUserReceived(HabitRPGUser user);

        void onUserFail();
    }
}
