package com.habitrpg.android.habitica.models.user;

import com.habitrpg.android.habitica.models.inventory.Egg;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.HatchingPotion;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.QuestContent;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Items extends RealmObject {

    @PrimaryKey
    private String userId;
    public RealmList<Egg> eggs;
    public RealmList<Food> food;
    public RealmList<HatchingPotion> hatchingPotions;
    public RealmList<QuestContent> quests;
    User user;
    RealmList<Pet> pets;
    RealmList<Mount> mounts;
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

    public RealmList<Egg> getEggs() {
        return eggs;
    }

    public void setEggs(RealmList<Egg> eggs) {
        this.eggs = eggs;
    }

    public RealmList<Food> getFood() {
        return food;
    }

    public void setFood(RealmList<Food> food) {
        this.food = food;
    }

    public RealmList<HatchingPotion> getHatchingPotions() {
        return hatchingPotions;
    }

    public void setHatchingPotions(RealmList<HatchingPotion> hatchingPotions) {
        this.hatchingPotions = hatchingPotions;
    }

    public RealmList<QuestContent> getQuests() {
        return quests;
    }

    public void setQuests(RealmList<QuestContent> quests) {
        this.quests = quests;
    }

    public RealmList<Pet> getPets() {
        return pets;
    }

    public void setPets(RealmList<Pet> pets) {
        this.pets = pets;
    }

    public RealmList<Mount> getMounts() {
        return mounts;
    }

    public void setMounts(RealmList<Mount> mounts) {
        this.mounts = mounts;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        if (gear != null && !gear.isManaged()) {
            gear.setUserId(userId);
        }
        if (special != null && !special.isManaged()) {
            special.setUserId(userId);
        }
    }
}
