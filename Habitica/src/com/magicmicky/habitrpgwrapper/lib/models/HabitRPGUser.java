package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Habit;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Reward;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ToDo;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

/**
 * Created by MagicMicky on 10/06/2014.
 */

@Table(databaseName = HabitDatabase.NAME)
public class HabitRPGUser extends BaseModel {

    @Column
    @PrimaryKey
    private String id;

    List<Daily> dailys;
    List<ToDo> todos;
    List<Reward> rewards;
    List<Habit> habits;
    List<Tag> tags;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "stats_id",
            columnType = Long.class,
            foreignColumnName = "id")})
    private Stats stats;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "preferences_id",
            columnType = Long.class,
            foreignColumnName = "id")})
    private Preferences preferences;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "profile_id",
                                                  columnType = Long.class,
                                                  foreignColumnName = "id")})
    private Profile profile;


    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "party_id",
            columnType = String.class,
            foreignColumnName = "id")})
    private Party party;


    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "items_id",
            columnType = Long.class,
            foreignColumnName = "id")})
    private Items items;

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDailys(List<Daily> dailys) {
        this.dailys = dailys;
    }

    public void setTodos(List<ToDo> todos) {
        this.todos = todos;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }

    public void setHabits(List<Habit> habits) {
        this.habits = habits;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public Items getItems() {
        return items;
    }

    public void setItems(Items items) {
        this.items = items;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "habits")
    public List<Habit> getHabits() {
        if(habits == null) {
            habits = new Select()
                    .from(Habit.class)
                    .queryList();
        }
        return habits;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "dailys")
    public List<Daily> getDailys() {
        if(dailys == null) {
            dailys = new Select()
                    .from(Daily.class)
                    .queryList();
        }
        return dailys;
}

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "todos")
    public List<ToDo> getTodos() {
        if(todos == null) {
            todos = new Select()
                    .from(ToDo.class)
                    .queryList();
        }
        return todos;
    }


    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "rewards")
    public List<Reward> getRewards() {
        if(rewards == null) {
            rewards = new Select()
                    .from(Reward.class)
                    .queryList();
        }
        return rewards;
    }

}
