package com.magicmicky.habitrpgwrapper.lib.models.responses;

import com.magicmicky.habitrpgwrapper.lib.models.Notification;

import java.util.List;

/**
 * Created by krh12 on 11/23/2016.
 */

public class HabitResponse<T> {

    private Boolean success;
    public T data;
    public List<Notification> notifications;

    /**
     *
     * @return
     * The success
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     *
     * @param success
     * The success
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     *
     * @return
     * The data
     */
    public T getData() {
        return data;
    }
}

