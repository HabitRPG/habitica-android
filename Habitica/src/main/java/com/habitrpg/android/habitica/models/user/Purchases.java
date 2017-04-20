package com.habitrpg.android.habitica.models.user;

import com.habitrpg.android.habitica.HabitDatabase;
import com.habitrpg.android.habitica.models.inventory.Customization;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

/**
 * Created by viirus on 14/01/16.
 */
@Table(databaseName = HabitDatabase.NAME)
public class Purchases extends BaseModel {


    public List<Customization> customizations;
    @Column
    @PrimaryKey
    @NotNull
    String user_id;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "plan_user_id",
            columnType = String.class,
            foreignColumnName = "customerId")})
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
