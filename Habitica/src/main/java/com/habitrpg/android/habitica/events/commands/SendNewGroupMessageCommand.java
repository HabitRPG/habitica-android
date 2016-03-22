package com.habitrpg.android.habitica.events.commands;

/**
 * Created by Negue on 24.08.2015.
 */
public class SendNewGroupMessageCommand {
    public String Message;
    public String TargetGroupId;

    public SendNewGroupMessageCommand(String targetGroupId, String message) {
        TargetGroupId = targetGroupId;
        Message = message;
    }
}
