package com.habitrpg.android.habitica.events.commands;

import com.habitrpg.android.habitica.models.social.ChatMessage;

/**
 * Created by jjbillings on 7/27/16.
 */
public class CopyChatMessageCommand extends ChatMessageCommandBase {
    public CopyChatMessageCommand(String groupId, ChatMessage chatMessage) {
        super(groupId, chatMessage);
    }
}
