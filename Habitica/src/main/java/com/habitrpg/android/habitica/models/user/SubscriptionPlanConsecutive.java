package com.habitrpg.android.habitica.models.user;

import io.realm.RealmObject;

public class SubscriptionPlanConsecutive extends RealmObject {

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
}
