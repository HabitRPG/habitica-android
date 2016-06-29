package com.habitrpg.android.habitica.helpers.notifications;

import android.content.Context;

/**
 * Created by keithholliday on 6/28/16.
 */
public interface HabiticaLocalNotification {

    void notifyLocally(Context context, String title, String message);

}
