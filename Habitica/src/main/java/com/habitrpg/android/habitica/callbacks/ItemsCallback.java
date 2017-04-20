package com.habitrpg.android.habitica.callbacks;

import com.habitrpg.android.habitica.models.user.HabitRPGUser;
import com.habitrpg.android.habitica.models.user.Items;

import rx.functions.Action1;

public class ItemsCallback implements Action1<Items> {

    private final HabitRPGUserCallback.OnUserReceived mCallback;

    private HabitRPGUser user;

    public ItemsCallback(HabitRPGUserCallback.OnUserReceived callback, HabitRPGUser user) {
        this.mCallback = callback;
        this.user = user;
    }

    @Override
    public void call(Items items) {
        this.user.setItems(items);
        this.user.async().save();
        mCallback.onUserReceived(this.user);
    }
}
