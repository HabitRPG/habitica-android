package com.habitrpg.android.habitica.callbacks;

import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.models.user.Items;

import rx.functions.Action1;

public class ItemsCallback implements Action1<Items> {

    private final HabitRPGUserCallback.OnUserReceived mCallback;

    private User user;

    public ItemsCallback(HabitRPGUserCallback.OnUserReceived callback, User user) {
        this.mCallback = callback;
        this.user = user;
    }

    @Override
    public void call(Items items) {
        this.user.setItems(items);
        mCallback.onUserReceived(this.user);
    }
}
