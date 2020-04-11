package com.habitrpg.android.habitica.models.user;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Purchases extends RealmObject {

    @PrimaryKey
    private String userId;

    public RealmList<OwnedCustomization> customizations;
    User user;
    private SubscriptionPlan plan;

    public List<OwnedCustomization> getCustomizations() {
        return customizations;
    }

    public void setCustomizations(RealmList<OwnedCustomization> customizations) {
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
