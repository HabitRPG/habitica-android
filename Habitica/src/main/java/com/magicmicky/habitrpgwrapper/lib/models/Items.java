package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Egg;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Food;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.HatchingPotion;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by MagicMicky on 16/03/14.
 */

@Table(databaseName = HabitDatabase.NAME)
public class Items extends BaseModel {

    @Column
    @PrimaryKey
    @NotNull
    String user_id;

    @Column
    private String currentMount, currentPet;

    @Column
    private int lastDrop_count;

    @Column
    private Date lastDrop_date;

    public List<Egg> eggs;
    public List<Food> food;
    public List<HatchingPotion> hatchingPotions;
    public List<QuestContent> quests;

    HashMap<String, Integer> pets;
    HashMap<String, Boolean> mounts;

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

    public List<Egg> getEggs() {
        return eggs;
    }

    public void setEggs(List<Egg> eggs) {
        this.eggs = eggs;
    }

    public List<Food> getFood() {
        return food;
    }

    public void setFood(List<Food> food) {
        this.food = food;
    }

    public List<HatchingPotion> getHatchingPotions() {
        return hatchingPotions;
    }

    public void setHatchingPotions(List<HatchingPotion> hatchingPotions) {
        this.hatchingPotions = hatchingPotions;
    }

    public List<QuestContent> getQuests() {
        return quests;
    }

    public void setQuests(List<QuestContent> quests) {
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

    public Items() {}

    @Override
    public void save() {
        gear.user_id = user_id;

        List<BaseModel> items = new ArrayList<>();
        items.addAll(this.quests);
        items.addAll(this.eggs);
        items.addAll(this.food);
        items.addAll(this.hatchingPotions);
        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(items)));

        super.save();
    }
}
