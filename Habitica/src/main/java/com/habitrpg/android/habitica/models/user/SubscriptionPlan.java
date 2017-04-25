package com.habitrpg.android.habitica.models.user;

import android.support.annotation.Nullable;


import java.util.Date;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class SubscriptionPlan extends RealmObject {

    public static String PLANID_BASIC = "basic";
    public static String PLANID_BASICEARNED = "basic_earned";
    public static String PLANID_BASIC3MONTH = "basic_3mo";
    public static String PLANID_BASIC6MONTH = "basic_6mo";
    public static String PLANID_GOOGLE6MONTH = "google_6mo";
    public static String PLANID_BASIC12MONTH = "basic_12mo";

    @PrimaryKey
    private String customerId;
    public Date dateCreated;
    public Date dateUpdated;
    @Nullable
    public Date dateTerminated;
    @Nullable
    public String paymentMethod;
    @Nullable
    public String planId;
    public Integer gemsBought;
    public Integer extraMonths;
    public Integer quantity;
    public SubscriptionPlanConsecutive consecutive;

    @Ignore
    public List<String> mysteryItems;

    public boolean isActive() {
        Date today = new Date();
        return planId != null && (this.dateTerminated == null || this.dateTerminated.after(today));
    }

    public Integer numberOfGemsLeft() {
        if (customerId == null) {
            return 0;
        }
        return 25 + consecutive.getGemCapExtra() - gemsBought;
    }


    public void setCustomerId(String customerId) {
        this.customerId = customerId;
        if (consecutive != null && !consecutive.isManaged()) {
            consecutive.setCustomerId(customerId);
        }
    }
}
