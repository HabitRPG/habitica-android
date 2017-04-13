package com.habitrpg.android.habitica.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

@Table(databaseName = HabitDatabase.NAME)
public class SubscriptionPlan extends BaseModel {

    public static String PLANID_BASIC = "basic";
    public static String PLANID_BASICEARNED = "basic_earned";
    public static String PLANID_BASIC3MONTH = "basic_3mo";
    public static String PLANID_BASIC6MONTH = "basic_6mo";
    public static String PLANID_GOOGLE6MONTH = "google_6mo";
    public static String PLANID_BASIC12MONTH = "basic_12mo";

    @Column
    @PrimaryKey
    public String customerId;
    @Column
    public Date dateCreated;
    @Column
    public Date dateUpdated;
    @Column
    @Nullable
    public Date dateTerminated;
    @Column
    @Nullable
    public String paymentMethod;
    @Column
    @Nullable
    public String planId;
    @Column
    public Integer gemsBought;
    @Column
    public Integer extraMonths;
    @Column
    public Integer quantity;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "consecutive_user_id",
            columnType = String.class,
            foreignColumnName = "customerId")})
    public SubscriptionPlanConsecutive consecutive;

    public List<String> mysteryItems;

    public boolean isActive() {
        Date today = new Date();
        return planId != null && (this.dateTerminated == null || this.dateTerminated.after(today));
    }

    @Override
    public void save() {

        if (consecutive != null) {
            consecutive.customerId = customerId;
        }

        super.save();
    }

    public Integer numberOfGemsLeft() {
        if (customerId == null) {
            return 0;
        }
        return 25 + consecutive.getGemCapExtra() - gemsBought;
    }


}
