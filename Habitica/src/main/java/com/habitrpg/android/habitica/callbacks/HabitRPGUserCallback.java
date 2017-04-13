package com.habitrpg.android.habitica.callbacks;

import com.habitrpg.android.habitica.models.HabitRPGUser;

import android.support.annotation.Nullable;

import rx.functions.Action1;

/**
 * r
 * Created by magicmicky on 18/02/15.
 */
public class HabitRPGUserCallback implements Action1<HabitRPGUser> {

    @Nullable
    public final OnUserReceived callBack;

    public HabitRPGUserCallback(@Nullable OnUserReceived callback) {
        this.callBack = callback;
    }

    @Override
    public void call(HabitRPGUser user) {
        // Negue: once everything is refactored to DbFlowTaskLocalRepository, this will be removed
        user.async().save();
        if (callBack != null) {
            callBack.onUserReceived(user);
        }
    }

    public interface OnUserReceived {
        void onUserReceived(HabitRPGUser user);
    }
}
