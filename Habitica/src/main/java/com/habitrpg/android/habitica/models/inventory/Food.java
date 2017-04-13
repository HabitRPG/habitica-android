package com.habitrpg.android.habitica.models.inventory;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

@Table(databaseName = HabitDatabase.NAME)
public class Food extends Item {

    @Column
    String target, article;

    @Column
    Boolean canDrop;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public Boolean getCanDrop() {
        return canDrop;
    }

    public void setCanDrop(Boolean canDrop) {
        this.canDrop = canDrop;
    }

    public String getType() {
        return "food";
    }
}
