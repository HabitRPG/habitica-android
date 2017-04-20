package com.habitrpg.android.habitica.models.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.HabitDatabase;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by keithholliday on 6/20/16.
 */
@Table(databaseName = HabitDatabase.NAME)
public class Inbox extends BaseModel {

    @Column
    @PrimaryKey
//    @NotNull
            String user_Id;

    @SerializedName("optOut")
    @Expose
    private Boolean optOut;

    @SerializedName("messages")
    @Expose
    private Map<String, ChatMessage> messages;

    @SerializedName("blocks")
    @Expose
    private List<Object> blocks = new ArrayList<Object>();

    @SerializedName("newMessages")
    @Expose
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
    public Map<String, ChatMessage> getMessages() {
        return messages;
    }

    /**
     * @param messages The messages
     */
    public void setMessages(Map<String, ChatMessage> messages) {
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

}