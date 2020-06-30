package com.habitrpg.android.habitica.models.responses;

import com.habitrpg.android.habitica.models.Notification;

import java.util.List;

/**
 * Created by krh12 on 11/23/2016.
 */

public class HabitResponse<T> {

    public T data;
    public List<Notification> notifications;
    private Boolean success;
    public String message;

    /**
     * @return The success
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * @param success The success
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     * @return The data
     */
    public T getData() {
        return data;
    }
}

