package com.habitrpg.android.habitica.models.user;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SubscriptionPlan extends RealmObject {

    public static String PLANID_BASIC = "basic";
    public static String PLANID_BASICEARNED = "basic_earned";
    public static String PLANID_BASIC3MONTH = "basic_3mo";
    public static String PLANID_BASIC6MONTH = "basic_6mo";
    public static String PLANID_GOOGLE6MONTH = "google_6mo";
    public static String PLANID_BASIC12MONTH = "basic_12mo";

    @PrimaryKey
    public String customerId;
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
    @Nullable
    public SubscriptionPlanConsecutive consecutive;

    public int mysteryItemCount;
    @SerializedName("owner")
    public String ownerID;

    public boolean isGroupPlanSub() {
        return customerId.equals("group-plan");
    }

    public boolean isGiftedSub() {
        return customerId.equals("Gift");
    }

    public boolean isActive() {
        Date today = new Date();
        return customerId != null && (this.dateTerminated == null || this.dateTerminated.after(today));
    }

    public int totalNumberOfGems() {
        if (customerId == null || consecutive == null) {
            return 0;
        }
        return 25 + consecutive.getGemCapExtra();
    }

    public int numberOfGemsLeft() {
        return totalNumberOfGems() - gemsBought;
    }



    public void setCustomerId(String customerId) {
        this.customerId = customerId;
        if (consecutive != null && !consecutive.isManaged()) {
            consecutive.setCustomerId(customerId);
        }
    }
}
