package com.habitrpg.shared.habitica.models.user;

import com.habitrpg.shared.habitica.models.user.SubscriptionPlan;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SubscriptionPlanConsecutive extends RealmObject {

    @PrimaryKey
    private String customerId;

    SubscriptionPlan subscriptionPlan;
    private int trinkets;
    private int gemCapExtra;
    private int offset;
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
