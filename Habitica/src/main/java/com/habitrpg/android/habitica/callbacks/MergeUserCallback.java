package com.habitrpg.android.habitica.callbacks;

import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;


public class MergeUserCallback extends HabitRPGUserCallback {

    private HabitRPGUser user;

    public MergeUserCallback(HabitRPGUserCallback.OnUserReceived callback, HabitRPGUser user) {
        super(callback);
        this.user = user;
    }

    @Override
    public void call(HabitRPGUser user) {
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

        callBack.onUserReceived(this.user);
    }
}
