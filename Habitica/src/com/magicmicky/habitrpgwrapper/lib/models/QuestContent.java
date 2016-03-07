package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Negue on 29.09.2015.
 */
@Table(databaseName = HabitDatabase.NAME)
public class QuestContent extends BaseModel {
    @PrimaryKey
    @Column
    public String key;

    @Column
    public String text;

    @Column
    public String notes;

    @Column
    public double value;

    @Column
    public String previous;

    @Column
    public int lvl;

    @Column
    public boolean canBuy;

    @Column
    public String category;

    public QuestBoss boss;

    HashMap<String, QuestCollect> collect;

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "collect")
    public Collection<QuestCollect> getCollectCollection() {
        return getCollect().values();
    }

    public HashMap<String, QuestCollect> getCollect() {
        if (collect == null) {
            List<QuestCollect> collectList = new Select()
                    .from(QuestCollect.class)
                    .where(Condition.column("quest_key").eq(this.key))
                    .queryList();
            collect = new HashMap<>();
            for (QuestCollect c : collectList) {
                collect.put(c.key, c);
            }
        }
        return collect;
    }

    public void setCollect(HashMap<String, QuestCollect> collect) {
        this.collect = collect;
    }

    public void save() {
        if (collect != null) {
            for (Map.Entry<String, QuestCollect> kv : collect.entrySet()) {
                kv.getValue().quest_key = key;
                kv.getValue().key = kv.getKey();
            }
        }
        super.save();
    }

    // todo drops
}

