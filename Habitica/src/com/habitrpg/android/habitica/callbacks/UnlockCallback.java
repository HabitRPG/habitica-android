package com.habitrpg.android.habitica.callbacks;

import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.responses.UnlockResponse;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by magicmicky on 18/02/15.
 */
public class UnlockCallback implements Callback<UnlockResponse> {

    private final HabitRPGUserCallback.OnUserReceived mCallback;

    private HabitRPGUser user;

    public UnlockCallback(HabitRPGUserCallback.OnUserReceived callback, HabitRPGUser user) {
        this.mCallback = callback;
        this.user = user;
    }

    @Override
    public void success(UnlockResponse unlockResponse, Response response) {
        this.user.setPurchased(unlockResponse.purchased);
        this.user.setItems(unlockResponse.items);
        this.user.setPreferences(unlockResponse.preferences);

        this.user.async().save();

        mCallback.onUserReceived(this.user);
    }

    @Override
    public void failure(RetrofitError error) {
        mCallback.onUserFail();
    }
}
