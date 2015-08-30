package com.habitrpg.android.habitica.events.commands;

import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;

/**
 * Created by Negue on 30.08.2015.
 */
public class DeleteChatMessageCommand {
    public String groupId;
    public ChatMessage chatMessage;

    public DeleteChatMessageCommand(String groupId, ChatMessage chatMessage){
        this.groupId = groupId;
        this.chatMessage = chatMessage;
    }
}
