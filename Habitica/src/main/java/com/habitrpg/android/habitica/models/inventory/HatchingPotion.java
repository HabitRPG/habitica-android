package com.habitrpg.android.habitica.models.inventory;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

@Table(databaseName = HabitDatabase.NAME)
public class HatchingPotion extends Item {

    @Column
    Boolean limited, premium;

    public Boolean getLimited() {
        return limited;
    }

    public void setLimited(Boolean limited) {
        this.limited = limited;
    }

    public Boolean getPremium() {
        return premium;
    }

    public void setPremium(Boolean premium) {
        this.premium = premium;
    }

    @Override
    public String getType() {
        return "hatchingPotions";
    }
}
