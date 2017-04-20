package com.habitrpg.android.habitica.events.commands;

/**
 * Created by keithholliday on 6/22/16.
 */
public class SendNewInboxMessageCommand {
    public String message;
    public String userToSendTo;

    public SendNewInboxMessageCommand(String userToSendTo, String message) {
        this.userToSendTo = userToSendTo;
        this.message = message;
    }
}
