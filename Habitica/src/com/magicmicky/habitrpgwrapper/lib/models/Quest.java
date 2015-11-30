package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.HashMap;

/**
 * Created by viirus on 06/07/15.
 */
@Table(databaseName = HabitDatabase.NAME)
public class Quest extends BaseModel {
    @Column
    @PrimaryKey
    public String key;

    @Column
    public boolean active;

    @Column
    public String leader;

    public HashMap<String, Boolean> members;

    @ForeignKey(references = {@ForeignKeyReference(columnName = "progress_id",
            columnType = Long.class,
            foreignColumnName = "id")})
    private QuestProgress progress;

    private Quest(String key, QuestProgress progress) {
        this.key = key;
        this.progress = progress;
    }

    public Quest() {
    }

    public QuestProgress getProgress() {
        return progress;
    }

    public void setProgress(QuestProgress progress) {
        this.progress = progress;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}