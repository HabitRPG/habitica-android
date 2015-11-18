package com.magicmicky.habitrpgwrapper.lib.models;

import com.google.gson.annotations.SerializedName;
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
    private double balance;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "stats_id",
            columnType = String.class,
            foreignColumnName = "id")})
    private Stats stats;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "preferences_id",
            columnType = String.class,
            foreignColumnName = "user_id")})
    private Preferences preferences;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "profile_id",
            columnType = String.class,
            foreignColumnName = "user_Id")})
    private Profile profile;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "party_id",
            columnType = String.class,
            foreignColumnName = "id")})
    private UserParty party;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "items_id",
            columnType = String.class,
            foreignColumnName = "user_id")})
    private Items items;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "authentication_id",
            columnType = String.class,
            foreignColumnName = "user_id")})
    @SerializedName("auth")
    private Authentication authentication;

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

    public UserParty getParty() {
        return party;
    }

    public void setParty(UserParty party) {
        this.party = party;
    }

    public Items getItems() {
        return items;
    }

    public void setItems(Items items) {
        this.items = items;
    }

    public void setBalance(double balance) {
        this.balance=balance;
    }

    public double getBalance() {
        return this.balance;
    }

    public Authentication getAuthentication() { return authentication; }

    public void setAuthentication(Authentication authentication) {this.authentication = authentication; }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "habits")
    public List<Task> getHabits() {
        if(habits == null) {
            habits = new Select()
                    .from(Task.class)
                    .where(Condition.column("type").eq("habit"))
                    .and(Condition.column("user_id").eq(this.id))
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
                    .and(Condition.column("user_id").eq(this.id))
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
                    .and(Condition.column("user_id").eq(this.id))
                    .queryList();
        }
        return todos;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "rewards")
    public List<Task> getRewards() {
        if(rewards == null) {
            rewards = new Select()
                    .from(Task.class)
                    .where(Condition.column("type").eq("reward"))
                    .and(Condition.column("user_id").eq(this.id))
                    .queryList();
        }
        return rewards;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "tags")
    public List<Tag> getTags() {
        if(tags == null) {
            tags = new Select()
                    .from(Tag.class)
                    .where(Condition.column("user_id").eq(this.id))
                    .queryList();
        }
        return tags;
    }

    @Override
    public void save() {
        // We need to set the user_id to all other objects
        preferences.user_id = id;
        stats.id = id;
        profile.user_Id = id;
        items.user_id = id;
        authentication.user_id = id;


        ArrayList<Task> allTasks = new ArrayList<Task>();
        allTasks.addAll(dailys);
        allTasks.addAll(todos);
        allTasks.addAll(habits);
        allTasks.addAll(rewards);

        for (Task t : allTasks) {
            t.user_id = id;
        }

        for (Tag t : tags) {
            t.user_id = id;
        }

        super.save();
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
            String armor = outfit.getArmor();

            if (armor != null && !armor.equals("armor_base_0")) {
                layerNames.add(prefs.getSize() + "_" + armor);
            }
            if (outfit.getBody() != null && !outfit.getBody().equals("body_base_0")) {
                layerNames.add(outfit.getBody());
            }
        }

        Hair hair = prefs.getHair();
        if (hair != null) {
            String hairColor = hair.getColor();

            if (hair.getBase() > 0) {layerNames.add("hair_base_"+hair.getBase() +"_" + hairColor);}
            if (hair.getBangs() > 0) {layerNames.add("hair_bangs_"+hair.getBangs() +"_" + hairColor);}
            if (hair.getMustache() > 0) {layerNames.add("hair_mustache_"+hair.getMustache() +"_" + hairColor);}
            if (hair.getBeard() > 0) {layerNames.add("hair_beard_"+hair.getBeard() +"_" + hairColor);}
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
