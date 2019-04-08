package com.habitrpg.android.habitica.models.inventory;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Food extends RealmObject implements Item {

    @PrimaryKey
    String key;
    String text, notes;
    Integer value;
    String target, article;
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

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNotes() {
        return notes;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
