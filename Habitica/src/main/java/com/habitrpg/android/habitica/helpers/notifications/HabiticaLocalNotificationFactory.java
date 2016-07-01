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

        if (notificationType.equalsIgnoreCase(PushNotificationManager.PARTY_INVITE_PUSH_NOTIFICATION_KEY)) {
            return new PartyInviteLocalNotification();
        } else if (notificationType.contains(PushNotificationManager.RECEIVED_PRIVATE_MESSAGE_PUSH_NOTIFICATION_KEY)) {
            return new ReceivedPrivateMessageLocalNotification();
        } else if (notificationType.contains(PushNotificationManager.RECEIVED_GEMS_PUSH_NOTIFICATION_KEY)) {
            return new ReceivedGemsGiftLocalNotification();
        }

        return null;
    }

}
