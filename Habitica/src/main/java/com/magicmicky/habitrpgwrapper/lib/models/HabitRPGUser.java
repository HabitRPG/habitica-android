package com.magicmicky.habitrpgwrapper.lib.models;

import com.google.gson.annotations.SerializedName;

import com.habitrpg.android.habitica.HabitDatabase;
import com.habitrpg.android.habitica.ui.AvatarView;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TasksOrder;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@Table(databaseName = HabitDatabase.NAME)
public class HabitRPGUser extends BaseModel {

    List<Task> dailys;
    List<Task> todos;
    List<Task> rewards;
    List<Task> habits;
    List<Tag> tags;
    @Column
    @PrimaryKey
    @SerializedName("_id")
    private String id;
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

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "flags_id",
            columnType = String.class,
            foreignColumnName = "user_id")})
    private Flags flags;

    private Purchases purchased;

    private TasksOrder tasksOrder;

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
        this.items.user_id = id;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public Purchases getPurchased() {
        return purchased;
    }

    public void setPurchased(Purchases purchased) {
        this.purchased = purchased;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "habits")
    public List<Task> getHabits() {
        if (habits == null) {
            habits = new Select()
                    .from(Task.class)
                    .where(Condition.column("type").eq("habit"))
                    .and(Condition.column("user_id").eq(this.id))
                    .queryList();
        }
        return habits;
    }

    public void setHabits(List<Task> habits) {
        this.habits = habits;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "dailys")
    public List<Task> getDailys() {
        if (dailys == null) {
            dailys = new Select()
                    .from(Task.class)
                    .where(Condition.column("type").eq("daily"))
                    .and(Condition.column("user_id").eq(this.id))
                    .queryList();
        }
        return dailys;
    }

    public void setDailys(List<Task> dailys) {
        this.dailys = dailys;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "todos")
    public List<Task> getTodos() {
        if (todos == null) {
            todos = new Select()
                    .from(Task.class)
                    .where(Condition.column("type").eq("todo"))
                    .and(Condition.column("user_id").eq(this.id))
                    .queryList();
        }
        return todos;
    }

    public void setTodos(List<Task> todos) {
        this.todos = todos;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "rewards")
    public List<Task> getRewards() {
        if (rewards == null) {
            rewards = new Select()
                    .from(Task.class)
                    .where(Condition.column("type").eq("reward"))
                    .and(Condition.column("user_id").eq(this.id))
                    .queryList();
        }
        return rewards;
    }

    public void setRewards(List<Task> rewards) {
        this.rewards = rewards;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "tags")
    public List<Tag> getTags() {
        if (tags == null) {
            tags = new Select()
                    .from(Tag.class)
                    .where(Condition.column("user_id").eq(this.id))
                    .queryList();
        }
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Flags getFlags() {
        return flags;
    }

    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    public TasksOrder getTasksOrder() {
        return tasksOrder;
    }

    public void setTasksOrder(TasksOrder tasksOrder) {
        this.tasksOrder = tasksOrder;
    }

    @Override
    public void save() {
        // We need to set the user_id to all other objects
        preferences.user_id = id;
        stats.id = id;
        profile.user_Id = id;
        items.user_id = id;
        authentication.user_id = id;
        flags.user_id = id;

        ArrayList<Task> allTasks = new ArrayList<Task>();
        if (dailys != null) {
            allTasks.addAll(dailys);
        }
        if (todos != null) {
            allTasks.addAll(todos);
        }
        if (habits != null) {
            allTasks.addAll(habits);
        }
        if (rewards != null) {
            allTasks.addAll(rewards);
        }

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

        if (prefs.getChair() != null) {
            layerNames.add(prefs.getChair());
        }

        Outfit outfit = null;
        if (this.getItems() != null) {
            if (prefs.getCostume()) {
                outfit = this.getItems().getGear().getCostume();
            } else {
                outfit = this.getItems().getGear().getEquipped();
            }
        }

        if (outfit != null) {
            if (outfit.getBack() != null) {
                layerNames.add(outfit.getBack());
            }
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

            if (hair.getBase() > 0) {
                layerNames.add("hair_base_" + hair.getBase() + "_" + hairColor);
            }
            if (hair.getBangs() > 0) {
                layerNames.add("hair_bangs_" + hair.getBangs() + "_" + hairColor);
            }
            if (hair.getMustache() > 0) {
                layerNames.add("hair_mustache_" + hair.getMustache() + "_" + hairColor);
            }
            if (hair.getBeard() > 0) {
                layerNames.add("hair_beard_" + hair.getBeard() + "_" + hairColor);
            }
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

    public EnumMap<AvatarView.LayerType, String> getAvatarLayerMap() {
        EnumMap<AvatarView.LayerType, String> layerMap = new EnumMap<>(AvatarView.LayerType.class);

        Preferences prefs = getPreferences();
        Outfit outfit = (prefs.getCostume()) ? getItems().getGear().getCostume() : getItems().getGear().getEquipped();

        if (!TextUtils.isEmpty(prefs.getChair())) {
            layerMap.put(AvatarView.LayerType.CHAIR, prefs.getChair());
        }

        if (outfit != null) {
            if (!TextUtils.isEmpty(outfit.getBack())) {
                layerMap.put(AvatarView.LayerType.BACK, outfit.getBack());
            }
            if (outfit.isAvailable(outfit.getArmor())) {
                layerMap.put(AvatarView.LayerType.ARMOR, prefs.getSize() + "_" + outfit.getArmor());
            }
            if (outfit.isAvailable(outfit.getBody())) {
                layerMap.put(AvatarView.LayerType.BODY, outfit.getBody());
            }
            if (outfit.isAvailable(outfit.getEyeWear())) {
                layerMap.put(AvatarView.LayerType.EYEWEAR, outfit.getEyeWear());
            }
            if (outfit.isAvailable(outfit.getHead())) {
                layerMap.put(AvatarView.LayerType.HEAD, outfit.getHead());
            }
            if (outfit.isAvailable(outfit.getHeadAccessory())) {
                layerMap.put(AvatarView.LayerType.HEAD_ACCESSORY, outfit.getHeadAccessory());
            }
            if (outfit.isAvailable(outfit.getShield())) {
                layerMap.put(AvatarView.LayerType.SHIELD, outfit.getShield());
            }
            if (outfit.isAvailable(outfit.getWeapon())) {
                layerMap.put(AvatarView.LayerType.WEAPON, outfit.getWeapon());
            }
        }

        layerMap.put(AvatarView.LayerType.SKIN, "skin_" + prefs.getSkin() + ((prefs.getSleep()) ? "_sleep" : ""));
        layerMap.put(AvatarView.LayerType.SHIRT, prefs.getSize() + "_shirt_" + prefs.getShirt());
        layerMap.put(AvatarView.LayerType.HEAD_0, "head_0");

        Hair hair = prefs.getHair();
        if (hair != null) {
            String hairColor = hair.getColor();

            if (hair.isAvailable(hair.getBase())) {
                layerMap.put(AvatarView.LayerType.HAIR_BASE, "hair_base_" + hair.getBase() + "_" + hairColor);
            }
            if (hair.isAvailable(hair.getBangs())) {
                layerMap.put(AvatarView.LayerType.HAIR_BANGS, "hair_bangs_" + hair.getBangs() + "_" + hairColor);
            }
            if (hair.isAvailable(hair.getMustache())) {
                layerMap.put(AvatarView.LayerType.HAIR_MUSTACHE, "hair_mustache_" + hair.getMustache() + "_" + hairColor);
            }
            if (hair.isAvailable(hair.getBeard())) {
                layerMap.put(AvatarView.LayerType.HAIR_BEARD, "hair_beard_" + hair.getBeard() + "_" + hairColor);
            }
            if (hair.isAvailable(hair.getFlower())) {
                layerMap.put(AvatarView.LayerType.HAIR_FLOWER, "hair_flower_" + hair.getFlower());
            }
        }

        if (prefs.getSleep()) {
            layerMap.put(AvatarView.LayerType.ZZZ, "zzz");
        }

        return layerMap;
    }
}
