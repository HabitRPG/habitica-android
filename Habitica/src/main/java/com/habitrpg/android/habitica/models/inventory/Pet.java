package com.habitrpg.android.habitica.models.inventory;


import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Pet extends RealmObject implements Animal{

    Integer trained;
    @PrimaryKey
    String key;
    String animal, color, animalGroup, text, type;
    boolean premium;

    @Ignore
    private Integer numberOwned;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    public String getAnimal() {
        if (animal == null) {
            return getKey().split("-")[0];
        }
        return animal;
    }

    public void setAnimal(String animal) {
        this.animal = animal;
    }

    public String getColor() {
        if (color == null) {
            return getKey().split("-")[1];
        }
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAnimalGroup() {
        if (animalGroup == null) {
            return "";
        }
        return animalGroup;
    }

    public void setAnimalGroup(String group) {
        this.animalGroup = group;
    }

    public boolean getPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public Integer getNumberOwned() {
        if (numberOwned == null) {
            return 0;
        }
        return numberOwned;
    }

    public void setNumberOwned(Integer numberOwned) {
        this.numberOwned = numberOwned;
    }
    public Integer getTrained() {
        if (trained == null) {
            return 0;
        }
        return trained;
    }

    public void setTrained(Integer trained) {
        this.trained = trained;
    }
}
