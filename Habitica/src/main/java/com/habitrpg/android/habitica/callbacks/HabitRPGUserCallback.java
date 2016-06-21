package com.habitrpg.android.habitica.callbacks;

import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import rx.functions.Action1;

/**
 * r
 * Created by magicmicky on 18/02/15.
 */
public class HabitRPGUserCallback implements Action1<HabitRPGUser> {

    public final OnUserReceived callBack;

    public HabitRPGUserCallback(OnUserReceived callback) {
        this.callBack = callback;
    }

    @Override
    public void call(HabitRPGUser habitRPGUser) {
        habitRPGUser.async().save();
        callBack.onUserReceived(habitRPGUser);
    }

    public interface OnUserReceived {
        void onUserReceived(HabitRPGUser user);
    }
}
