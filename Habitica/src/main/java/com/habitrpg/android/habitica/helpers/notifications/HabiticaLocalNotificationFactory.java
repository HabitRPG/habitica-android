package com.habitrpg.android.habitica.helpers.notifications;

/**
 * Created by keithholliday on 6/28/16.
 */
public class HabiticaLocalNotificationFactory {

    //use getShape method to get object of type shape
    public HabiticaLocalNotification build(String notificationType){
        if(notificationType == null){
            return null;
        }

        if(notificationType.equalsIgnoreCase("PARTY_INVITE_NOTIFICATION")){
            return new PartyInviteLocalNotification();

        }

        return null;
    }

}
