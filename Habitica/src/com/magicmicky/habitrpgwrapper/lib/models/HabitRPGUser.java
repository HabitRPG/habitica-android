package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
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

    List<Task> dailys;
    List<Task> todos;
    List<Task> rewards;
    List<Task> habits;
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

    public void setDailys(List<Task> dailys) {
        this.dailys = dailys;
    }

    public void setTodos(List<Task> todos) {
        this.todos = todos;
    }

    public void setRewards(List<Task> rewards) {
        this.rewards = rewards;
    }

    public void setHabits(List<Task> habits) {
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
    public List<Task> getHabits() {
        if(habits == null) {
            habits = new Select()
                    .from(Task.class)
                    .where(Condition.column("type").eq("habit"))
                    .queryList();
        }
        return habits;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "dailys")
    public List<Task> getDailys() {
        if(dailys == null) {
            dailys = new Select()
                    .from(Task.class)
                    .where(Condition.column("type").eq("daily"))
                    .queryList();
        }
        return dailys;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "todos")
    public List<Task> getTodos() {
        if(todos == null) {
            todos = new Select()
                    .from(Task.class)
                    .where(Condition.column("type").eq("todo"))
                    .queryList();
        }
        return todos;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "rewards")
    public List<Task> getRewards() {
        if(rewards == null) {
            rewards = new Select()
                    .from(Task.class)
                    .queryList();
        }
        return rewards;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "tags")
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

        Outfit outfit;
        if (prefs.getCostume()) {
            outfit = this.getItems().getGear().getCostume();
        } else {
            outfit = this.getItems().getGear().getEquipped();
        }

        if (outfit != null) {
            if (outfit.getBack() != null) {layerNames.add(outfit.getBack());}
        }

        if (prefs.getSleep()) {
            layerNames.add("skin_" + prefs.getSkin() + "_sleep");
        } else {
            layerNames.add("skin_" + prefs.getSkin());
        }
        layerNames.add(prefs.getSize() + "_shirt_" + prefs.getShirt());
        layerNames.add("head_0");

        if (outfit != null) {
            if (outfit.getArmor() != null && !outfit.getArmor().equals("armor_base_0")) {
                layerNames.add(prefs.getSize() + "_armor_" + outfit.getArmor());
            }
            if (outfit.getBody() != null && !outfit.getBody().equals("body_base_0")) {
                layerNames.add(outfit.getBody());
            }
        }

        Preferences.Hair hair = prefs.getHair();
        if (hair != null) {
            if (hair.getBase() > 0) {layerNames.add("hair_base_"+hair.getBase() + hair.getColor());}
            if (hair.getBangs() > 0) {layerNames.add("hair_bangs_"+hair.getBangs() + hair.getColor());}
            if (hair.getMustache() > 0) {layerNames.add("hair_mustache_"+hair.getMustache() + hair.getColor());}
            if (hair.getBeard() > 0) {layerNames.add("hair_beard_"+hair.getBeard() + hair.getColor());}
        }

        if (outfit != null) {
            if (outfit.getEyeWear() != null && !outfit.getEyeWear().equals("eyewear_base_0")) {
                layerNames.add(outfit.getEyeWear());
            }
            if (outfit.getHead() != null && !outfit.getHead().equals("head_base_0")) {
                layerNames.add(outfit.getHead());
            }
            if (outfit.getHeadAccessory() != null && !outfit.getHeadAccessory().equals("headAccessory_base_0")) {
                layerNames.add(outfit.getHeadAccessory());
            }
            if (outfit.getShield() != null && !outfit.getShield().equals("shield_base_0")) {
                layerNames.add(outfit.getShield());
            }
            if (outfit.getWeapon() != null && !outfit.getWeapon().equals("weapon_base_0")) {
                layerNames.add(outfit.getWeapon());
            }
        }

        if (prefs.getSleep()) {
            layerNames.add("zzz");
        }

        return layerNames;
    }
}
