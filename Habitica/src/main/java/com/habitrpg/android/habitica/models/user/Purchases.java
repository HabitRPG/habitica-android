package com.habitrpg.android.habitica.models.user;

import com.habitrpg.android.habitica.models.inventory.Customization;

import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Purchases extends RealmObject {

    @PrimaryKey
    private String userId;

    @Ignore
    public List<Customization> customizations;
    User user;
    private SubscriptionPlan plan;

    public List<Customization> getCustomizations() {
        return customizations;
    }

    public void setCustomizations(List<Customization> customizations) {
        this.customizations = customizations;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
