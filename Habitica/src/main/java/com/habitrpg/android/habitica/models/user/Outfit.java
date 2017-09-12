package com.habitrpg.android.habitica.models.user;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Outfit extends RealmObject {

    @PrimaryKey
    private String userId;

    Gear gear;
    String armor, back, body, head, shield, weapon;
    @SerializedName("eyewear")
    String eyeWear;
    String headAccessory;

    public String getArmor() {
        return armor;
    }

    public void setArmor(String armor) {
        this.armor = armor;
    }

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getEyeWear() {
        return eyeWear;
    }

    public void setEyeWear(String eyeWear) {
        this.eyeWear = eyeWear;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getHeadAccessory() {
        return headAccessory;
    }

    public void setHeadAccessory(String headAccessory) {
        this.headAccessory = headAccessory;
    }

    public String getShield() {
        return shield;
    }

    public void setShield(String shield) {
        this.shield = shield;
    }

    public String getWeapon() {
        return weapon;
    }

    public void setWeapon(String weapon) {
        this.weapon = weapon;
    }

    public boolean isAvailable(String outfit) {
        return !TextUtils.isEmpty(outfit) && !outfit.endsWith("base_0");
    }

    public void updateWith(Outfit newOutfit) {
        this.setArmor(newOutfit.getArmor());
        this.setBack(newOutfit.getBack());
        this.setBody(newOutfit.getBody());
        this.setEyeWear(newOutfit.getEyeWear());
        this.setHead(newOutfit.getHead());
        this.setHeadAccessory(newOutfit.getHeadAccessory());
        this.setShield(newOutfit.getShield());
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
