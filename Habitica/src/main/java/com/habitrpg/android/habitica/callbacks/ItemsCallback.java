package com.habitrpg.android.habitica.callbacks;

import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Items;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ItemsCallback implements Callback<Items> {

    private final HabitRPGUserCallback.OnUserReceived mCallback;

    private HabitRPGUser user;

    public ItemsCallback(HabitRPGUserCallback.OnUserReceived callback, HabitRPGUser user) {
        this.mCallback = callback;
        this.user = user;
    }

    @Override
    public void success(Items items, Response response) {
        this.user.setItems(items);

        this.user.async().save();

        mCallback.onUserReceived(this.user);
    }

    @Override
    public void failure(RetrofitError error) {
        mCallback.onUserFail();
    }
}
