package com.habitrpg.android.habitica.events.commands;

/**
 * Created by keithholliday on 6/22/16.
 */
public class SendNewInboxMessageCommand {
    public String Message;
    public String UserToSendTo;

    public SendNewInboxMessageCommand(String userToSendTo, String message) {
        UserToSendTo = userToSendTo;
        Message = message;
    }
}
