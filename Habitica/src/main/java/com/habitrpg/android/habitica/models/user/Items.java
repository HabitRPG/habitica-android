package com.habitrpg.android.habitica.models.user;

import java.util.Date;
import java.util.HashMap;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Items extends RealmObject {

    @Ignore
    public HashMap<String, Integer> eggs;
    @Ignore
    public HashMap<String, Integer> food;
    @Ignore
    public HashMap<String, Integer> hatchingPotions;
    @Ignore
    public HashMap<String, Integer> quests;
    User user;
    @Ignore
    HashMap<String, Integer> pets;
    @Ignore
    HashMap<String, Boolean> mounts;
    private String currentMount;
    private String currentPet;
    private int lastDrop_count;
    private Date lastDrop_date;

    //private QuestContent quest;
    private Gear gear;
    private SpecialItems special;

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

    public SpecialItems getSpecial() {
        return special;
    }

    public void setSpecial(SpecialItems specialItems) {
        this.special = specialItems;
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

}
