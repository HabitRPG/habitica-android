package com.habitrpg.android.habitica.callbacks;

import com.habitrpg.android.habitica.models.responses.UnlockResponse;
import com.habitrpg.android.habitica.models.user.User;

import rx.functions.Action1;

/**
 * Created by magicmicky on 18/02/15.
 */
public class UnlockCallback implements Action1<UnlockResponse> {

    private final HabitRPGUserCallback.OnUserReceived callback;

    private User user;

    public UnlockCallback(HabitRPGUserCallback.OnUserReceived callback, User user) {
        this.callback = callback;
        this.user = user;
    }

    @Override
    public void call(UnlockResponse unlockResponse) {
        this.user.setPurchased(unlockResponse.purchased);
        this.user.setItems(unlockResponse.items);
        this.user.setPreferences(unlockResponse.preferences);

        callback.onUserReceived(this.user);
    }
}
