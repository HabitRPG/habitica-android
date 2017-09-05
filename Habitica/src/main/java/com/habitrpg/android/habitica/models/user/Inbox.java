package com.habitrpg.android.habitica.models.user;

import com.habitrpg.android.habitica.models.social.ChatMessage;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Inbox extends RealmObject {

    @PrimaryKey
    private String userId;

    User user;
    private Boolean optOut;
    private RealmList<ChatMessage> messages;
    @Ignore
    private List<Object> blocks = new ArrayList<>();
    private Integer newMessages;

    /**
     * @return The optOut
     */
    public Boolean getOptOut() {
        return optOut;
    }

    /**
     * @param optOut The optOut
     */
    public void setOptOut(Boolean optOut) {
        this.optOut = optOut;
    }

    /**
     * @return The messages
     */
    public RealmList<ChatMessage> getMessages() {
        return messages;
    }

    /**
     * @param messages The messages
     */
    public void setMessages(RealmList<ChatMessage> messages) {
        this.messages = messages;
    }

    /**
     * @return The blocks
     */
    public List<Object> getBlocks() {
        return blocks;
    }

    /**
     * @param blocks The blocks
     */
    public void setBlocks(List<Object> blocks) {
        this.blocks = blocks;
    }

    /**
     * @return The newMessages
     */
    public Integer getNewMessages() {
        return newMessages;
    }

    /**
     * @param newMessages The newMessages
     */
    public void setNewMessages(Integer newMessages) {
        this.newMessages = newMessages;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}