package com.magicmicky.habitrpgwrapper.lib.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by keithholliday on 7/5/16.
 */
public class PushDevice {

    @SerializedName("regId")
    @Expose
    private String regId;

    @SerializedName("type")
    @Expose
    private String type;

    public String getRegId() {
        return this.regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
