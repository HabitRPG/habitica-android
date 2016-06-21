package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by MagicMicky on 16/03/14.
 */

@Table(databaseName = HabitDatabase.NAME)
public class Items extends BaseModel {

    public HashMap<String, Integer> eggs;
    public HashMap<String, Integer> food;
    public HashMap<String, Integer> hatchingPotions;
    public HashMap<String, Integer> quests;
    @Column
    @PrimaryKey
    @NotNull
    String user_id;
    HashMap<String, Integer> pets;
    HashMap<String, Boolean> mounts;
    @Column
    private String currentMount, currentPet;
    @Column
    private int lastDrop_count;
    @Column
    private Date lastDrop_date;

    //private QuestContent quest;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "gear_id",
            columnType = String.class,
            foreignColumnName = "user_id")})
    private Gear gear;

    public Items(String currentMount, String currentPet, int lastDrop_count, Date lastDrop_date) {
        this.currentMount = currentMount;
        this.currentPet = currentPet;
        this.lastDrop_count = lastDrop_count;
        this.lastDrop_date = lastDrop_date;
    }

    public Items() {
    }

    public String getCurrentMount() {
        return currentMount;
    }

    public void setCurrentMount(String currentMount) {
        this.currentMount = currentMount;
    }

    public String getCurrentPet() {
        return currentPet;
    }

    public void setCurrentPet(String currentPet) {
        this.currentPet = currentPet;
    }

    public int getLastDrop_count() {
        return lastDrop_count;
    }

    public void setLastDrop_count(int lastDrop_count) {
        this.lastDrop_count = lastDrop_count;
    }

    public Date getLastDrop_date() {
        return lastDrop_date;
    }

    public void setLastDrop_date(Date lastDrop_date) {
        this.lastDrop_date = lastDrop_date;
    }

    public Gear getGear() {
        return gear;
    }

    public void setGear(Gear gear) {
        this.gear = gear;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public HashMap<String, Integer> getEggs() {
        return eggs;
    }

    public void setEggs(HashMap<String, Integer> eggs) {
        this.eggs = eggs;
    }

    public HashMap<String, Integer> getFood() {
        return food;
    }

    public void setFood(HashMap<String, Integer> food) {
        this.food = food;
    }

    public HashMap<String, Integer> getHatchingPotions() {
        return hatchingPotions;
    }

    public void setHatchingPotions(HashMap<String, Integer> hatchingPotions) {
        this.hatchingPotions = hatchingPotions;
    }

    public HashMap<String, Integer> getQuests() {
        return quests;
    }

    public void setQuests(HashMap<String, Integer> quests) {
        this.quests = quests;
    }

    public HashMap<String, Integer> getPets() {
        return pets;
    }

    public void setPets(HashMap<String, Integer> pets) {
        this.pets = pets;
    }

    public HashMap<String, Boolean> getMounts() {
        return mounts;
    }

    public void setMounts(HashMap<String, Boolean> mounts) {
        this.mounts = mounts;
    }

    @Override
    public void save() {
        gear.user_id = user_id;
        super.save();
    }
}
