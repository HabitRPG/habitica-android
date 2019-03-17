package com.habitrpg.android.habitica.models.notifications;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Represents Habitica "Global notifications", i.e. the notifications about chat messages
 * (in Guilds and Party), Party & Quest invitations, unallocated stat points etc.
 *
 * These are different from other kind of notifications, such as Push notifications and
 * Popup notifications.
 */
public class GlobalNotification extends RealmObject {

    @PrimaryKey
    public String id;

    public String type;
    public boolean seen;
    // TODO data
}
