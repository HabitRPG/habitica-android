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
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
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

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "rewards")
    public List<Tag> getTags() {
        if(tags == null) {
            tags = new Select()
                    .from(Tag.class)
                    .queryList();
        }
        return tags;
    }


    public List<String> getAvatarLayerNames() {
        List<String> layerNames = new ArrayList<String>();

        Preferences prefs = this.getPreferences();

        if (prefs.getSleep()) {
            layerNames.add("skin_" + prefs.getSkin() + "_sleep");
        } else {
            layerNames.add("skin_" + prefs.getSkin());
        }
        layerNames.add(prefs.getSize() + "_shirt_" + prefs.getShirt());
        layerNames.add("head_0");

        Gear gear = this.getItems().getGear();

        Outfit outfit;

        if(gear != null) {
            if (prefs.getCostume()) {
                outfit = gear.getCostume();
            } else {
                outfit = gear.getEquipped();
            }
            if (outfit != null) {
                layerNames.add(outfit.getBack());
                layerNames.add(outfit.getEyeWear());
                layerNames.add(prefs.getSize() + "_armor_" + outfit.getArmor());
                layerNames.add(outfit.getBody());
            }

            Preferences.Hair hair = prefs.getHair();
            if (hair != null) {
                layerNames.add("hair_base_" + hair.getBase() + hair.getColor());
                layerNames.add("hair_bangs_" + hair.getBangs() + hair.getColor());
                layerNames.add("hair_mustache_" + hair.getMustache() + hair.getColor());
                layerNames.add("hair_beard_" + hair.getBeard() + hair.getColor());
            }

            if (outfit != null) {
                layerNames.add(outfit.getHead());
                layerNames.add(outfit.getHeadAccessory());
                layerNames.add(outfit.getShield());
                layerNames.add(outfit.getWeapon());
            }
        }

        if (prefs.getSleep()) {
            layerNames.add("zzz");
        }

        return layerNames;
    }
}
