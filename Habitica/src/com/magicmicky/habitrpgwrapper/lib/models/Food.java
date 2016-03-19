package com.magicmicky.habitrpgwrapper.lib.models;

import com.raizlabs.android.dbflow.annotation.Column;

public class Food extends BaseItem {

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
}
