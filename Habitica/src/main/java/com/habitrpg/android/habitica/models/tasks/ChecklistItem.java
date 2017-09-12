package com.habitrpg.android.habitica.models.tasks;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by viirus on 06/07/15.
 */
public class ChecklistItem extends RealmObject {

    @PrimaryKey
    private String id;
    private String text;
    private boolean completed;
    private int position;

    public ChecklistItem() {
        this(null, null);
    }

    public ChecklistItem(String id, String text) {
        this(id, text, false);
    }

    public ChecklistItem(String id, String text, boolean completed) {
        this.setText(text);
        if (id == null) {
            this.setId(UUID.randomUUID().toString());
        } else {
            this.setId(id);
        }
        this.setCompleted(completed);
    }

    public ChecklistItem(String text) {
        this(null, text);
    }

    public ChecklistItem(ChecklistItem item) {
        this.text = item.getText();
        this.id = item.getId();
        this.completed = item.getCompleted();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(ChecklistItem.class) && this.id != null) {
            return this.id.equals(((ChecklistItem)obj).id);
        }
        return super.equals(obj);
    }
}
