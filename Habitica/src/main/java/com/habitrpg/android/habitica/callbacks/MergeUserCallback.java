package com.habitrpg.android.habitica.callbacks;

import android.support.annotation.Nullable;

import com.habitrpg.android.habitica.models.user.User;


public class MergeUserCallback extends HabitRPGUserCallback {

    private User user;

    public MergeUserCallback(@Nullable HabitRPGUserCallback.OnUserReceived callback, User user) {
        super(callback);
        this.user = user;
    }

    @Override
    public void call(User user) {

        if (callBack != null) {
            callBack.onUserReceived(this.user);
        }
    }
}
