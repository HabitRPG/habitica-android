package com.habitrpg.android.habitica.models.inventory;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Table(databaseName = HabitDatabase.NAME)
public class QuestContent extends Item {

    @Column
    public String previous;

    @Column
    public int lvl;

    @Column
    public boolean canBuy;

    @Column
    public String category;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "boss_id",
            columnType = String.class,
            foreignColumnName = "key")})
    public QuestBoss boss;

    HashMap<String, QuestCollect> collect;

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public int getLvl() {
        return lvl;
    }

    public void setLvl(int lvl) {
        this.lvl = lvl;
    }

    public boolean isCanBuy() {
        return canBuy;
    }

    public void setCanBuy(boolean canBuy) {
        this.canBuy = canBuy;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public QuestBoss getBoss() {
        return boss;
    }

    public void setBoss(QuestBoss boss) {
        this.boss = boss;
    }

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

        if (boss != null) {
            boss.key = key;
        }

        if (collect != null) {
            for (Map.Entry<String, QuestCollect> kv : collect.entrySet()) {
                kv.getValue().quest_key = key;
                kv.getValue().key = kv.getKey();
            }
        }
        super.save();
    }

    @Override
    public String getType() {
        return "quests";
    }
}
