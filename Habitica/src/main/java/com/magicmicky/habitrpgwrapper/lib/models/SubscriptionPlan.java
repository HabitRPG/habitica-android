package com.magicmicky.habitrpgwrapper.lib.models;

import java.util.Date;

public class SubscriptionPlan {

    public static String PLANID_BASIC = "basic";
    public static String PLANID_BASICEARNED = "basic_earned";
    public static String PLANID_BASIC3MONTH = "basic_3mo";
    public static String PLANID_BASIC6MONTH = "basic_6mo";
    public static String PLANID_GOOGLE6MONTH = "google_6mo";
    public static String PLANID_BASIC12MONTH = "basic_12mo";

    public String customerId;
    public Date dateCreated;
    public Date dateUpdated;
    public Date dateTerminated;
    public String paymentMethod;
    public String planId;
    public Integer gemsBought;
    public Integer extraMonths;
    public Integer quantity;
    public SubscriptionPlanConsecutive consecutive;

    public boolean isActive() {
        Date today = new Date();
        return this.dateCreated.before(today) && (this.dateTerminated == null || this.dateTerminated.after(today));
    }
}
