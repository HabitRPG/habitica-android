package com.magicmicky.habitrpgwrapper.lib.models;

import java.util.List;

/**
 * Created by viirus on 14/01/16.
 */
public class Purchases {

    public List<Customization> customizations;
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
}
