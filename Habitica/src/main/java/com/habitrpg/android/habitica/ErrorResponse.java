package com.habitrpg.android.habitica;

import com.magicmicky.habitrpgwrapper.lib.models.HabiticaError;

import java.util.List;

public class ErrorResponse {
    public String message;
    public List<HabiticaError> errors;

    public String getDisplayMessage() {
        if (errors != null && errors.size() > 0) {
            HabiticaError error = errors.get(0);
            if (error.message != null && error.message.length() > 0) {
                return error.message;
            }
        }
        if (message != null && message.length() > 0) {
            return message;
        }
        return "";
    }
}
