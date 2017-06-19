package com.habitrpg.android.habitica.events.commands;

import com.habitrpg.android.habitica.models.social.ChatMessage;

/**
 * Created by Negue on 30.08.2015.
 */
public class CopyChatAsTodoCommand extends ChatMessageCommandBase {
    public CopyChatAsTodoCommand(String groupId, ChatMessage chatMessage) {
        super(groupId, chatMessage);
    }
}
