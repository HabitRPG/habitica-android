package com.habitrpg.android.habitica.events.commands;

/**
 * Created by Negue on 24.08.2015.
 */
public class SendNewGroupMessageCommand {
    public String message;
    public String targetGroupId;

    public SendNewGroupMessageCommand(String targetGroupId, String message) {
        this.targetGroupId = targetGroupId;
        this.message = message;
    }
}
