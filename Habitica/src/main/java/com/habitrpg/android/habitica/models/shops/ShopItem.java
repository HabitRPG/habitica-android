package com.habitrpg.android.habitica.models.shops;

import android.content.res.Resources;

import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;

public class ShopItem {

    public static final String GEM_FOR_GOLD = "gem";
    public String key;
    public String text;
    public String notes;
    @SerializedName("class")
    public String imageName;
    public Integer value;
    public Boolean locked;
    public String currency;
    public String purchaseType;
    public String categoryIdentifier;
    public Integer limitedNumberLeft;
    public ShopItemUnlockCondition unlockCondition;

    public static ShopItem makeGemItem(Resources res) {
        ShopItem item = new ShopItem();
        item.key = GEM_FOR_GOLD;
        item.text = res.getString(R.string.gem_shop);
        item.notes = res.getString(R.string.gem_for_gold_description);
        item.imageName = "gem_shop";
        item.value = 20;
        item.currency = "gold";
        item.purchaseType = "gems";
        return item;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImageName() {
        if (imageName != null) {
            return imageName;
        } else {
            return "shop_" + key;
        }
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Boolean getLocked() {
        if (locked == null) {
            return false;
        }
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }

    public String getCategoryIdentifier() {
        return categoryIdentifier;
    }

    public void setCategoryIdentifier(String categoryIdentifier) {
        this.categoryIdentifier = categoryIdentifier;
    }

    public Integer getLimitedNumberLeft() {
        return limitedNumberLeft;
    }

    public void setLimitedNumberLeft(Integer limitedNumberLeft) {
        this.limitedNumberLeft = limitedNumberLeft;
    }

    public ShopItemUnlockCondition getUnlockCondition() {
        return unlockCondition;
    }

    public void setUnlockCondition(ShopItemUnlockCondition unlockCondition) {
        this.unlockCondition = unlockCondition;
    }

    public boolean canBuy(HabitRPGUser user) {
        if (getCurrency().equals("gold")) {
            return getValue() <= user.getStats().getGp();
        } else if (getCurrency().equals("gems")) {
            return getValue() <= (user.getBalance() * 4);
        } else {
            return false;
        }
    }
}
