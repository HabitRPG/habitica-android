package com.habitrpg.android.habitica.callbacks;

import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import android.support.annotation.Nullable;


public class MergeUserCallback extends HabitRPGUserCallback {

    private HabitRPGUser user;

    public MergeUserCallback(@Nullable HabitRPGUserCallback.OnUserReceived callback, HabitRPGUser user) {
        super(callback);
        this.user = user;
    }

    @Override
    public void call(HabitRPGUser user) {

        if (callBack != null) {
            callBack.onUserReceived(this.user);
        }
    }
}
