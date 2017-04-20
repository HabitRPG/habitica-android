package com.habitrpg.android.habitica.models.user;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(databaseName = HabitDatabase.NAME)
public class SubscriptionPlanConsecutive extends BaseModel {

    @Column
    @PrimaryKey
    public String customerId;
    @Column
    private int trinkets;
    @Column
    private int gemCapExtra;
    @Column
    private int offset;
    @Column
    private int count;

    public int getTrinkets() {
        return trinkets;
    }

    public void setTrinkets(int trinkets) {
        this.trinkets = trinkets;
    }

    public int getGemCapExtra() {
        return gemCapExtra;
    }

    public void setGemCapExtra(int gemCapExtra) {
        this.gemCapExtra = gemCapExtra;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
