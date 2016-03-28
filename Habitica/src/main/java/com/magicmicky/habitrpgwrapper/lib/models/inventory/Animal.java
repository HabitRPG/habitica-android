package com.magicmicky.habitrpgwrapper.lib.models.inventory;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.BaseModel;

public class Animal extends BaseModel {

    @Column
    @PrimaryKey
    String key;

    @Column
    String animal, color, animalGroup;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAnimal() {
        return animal;
    }

    public void setAnimal(String animal) {
        this.animal = animal;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAnimalGroup() {
        return animalGroup;
    }

    public void setAnimalGroup(String group) {
        this.animalGroup = group;
    }

}
