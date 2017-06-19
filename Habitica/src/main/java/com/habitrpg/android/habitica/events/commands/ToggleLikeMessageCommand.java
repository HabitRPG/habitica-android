package com.habitrpg.android.habitica.events.commands;

import com.habitrpg.android.habitica.models.social.ChatMessage;

/**
 * Created by Negue on 02.09.2015.
 */
public class ToggleLikeMessageCommand extends ChatMessageCommandBase {
    public ToggleLikeMessageCommand(String groupId, ChatMessage chatMessage) {
        super(groupId, chatMessage);
    }
}
