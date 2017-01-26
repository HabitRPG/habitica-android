package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;

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
    public Date dateTerminated;
    @Column
    public String paymentMethod;
    @Column
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

    public boolean isActive() {
        Date today = new Date();
        return (this.dateCreated != null && this.dateCreated.before(today)) && (this.dateTerminated == null || this.dateTerminated.after(today));
    }

    @Override
    public void save() {

        if (consecutive != null) {
            consecutive.customerId = customerId;
        }

        super.save();
    }
}
