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
    private boolean optOut;
    @Ignore
    private List<Object> blocks = new ArrayList<>();
    private int newMessages;

    /**
     * @return The optOut
     */
    public boolean getOptOut() {
        return optOut;
    }

    /**
     * @param optOut The optOut
     */
    public void setOptOut(boolean optOut) {
        this.optOut = optOut;
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
    public int getNewMessages() {
        return newMessages;
    }

    /**
     * @param newMessages The newMessages
     */
    public void setNewMessages(int newMessages) {
        this.newMessages = newMessages;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}