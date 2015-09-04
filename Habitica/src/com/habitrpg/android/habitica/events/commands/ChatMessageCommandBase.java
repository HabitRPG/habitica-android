package com.habitrpg.android.habitica.events.commands;

import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;

/**
 * Created by Negue on 04.09.2015.
 */
public abstract class ChatMessageCommandBase {
    public String groupId;
    public ChatMessage chatMessage;

    public ChatMessageCommandBase(String groupId, ChatMessage chatMessage){
        this.groupId = groupId;
        this.chatMessage = chatMessage;
    }
}
