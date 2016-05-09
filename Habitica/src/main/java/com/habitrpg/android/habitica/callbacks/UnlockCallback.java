package com.habitrpg.android.habitica.callbacks;

import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.responses.UnlockResponse;

import rx.functions.Action1;

/**
 * Created by magicmicky on 18/02/15.
 */
public class UnlockCallback implements Action1<UnlockResponse> {

    private final HabitRPGUserCallback.OnUserReceived callback;

    private HabitRPGUser user;

    public UnlockCallback(HabitRPGUserCallback.OnUserReceived callback, HabitRPGUser user) {
        this.callback = callback;
        this.user = user;
    }

    @Override
    public void call(UnlockResponse unlockResponse) {
        this.user.setPurchased(unlockResponse.purchased);
        this.user.setItems(unlockResponse.items);
        this.user.setPreferences(unlockResponse.preferences);
        this.user.async().save();

        callback.onUserReceived(this.user);
    }
}
