package com.habitrpg.android.habitica.callbacks;

import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Items;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MergeUserCallback implements Callback<HabitRPGUser> {

    private final HabitRPGUserCallback.OnUserReceived mCallback;

    private HabitRPGUser user;

    public MergeUserCallback(HabitRPGUserCallback.OnUserReceived callback, HabitRPGUser user) {
        this.mCallback = callback;
        this.user = user;
    }

    @Override
    public void success(HabitRPGUser user, Response response) {
        if (user.getItems() != null) {
            this.user.setItems(user.getItems());
        }
        if (user.getPreferences() != null) {
            this.user.setPreferences(user.getPreferences());
        }
        if (user.getFlags() != null) {
            this.user.setFlags(user.getFlags());
        }
        if (user.getStats() != null) {
            this.user.setStats(user.getStats());
        }

        this.user.async().save();

        mCallback.onUserReceived(this.user);
    }

    @Override
    public void failure(RetrofitError error) {
        mCallback.onUserFail();
    }
}
