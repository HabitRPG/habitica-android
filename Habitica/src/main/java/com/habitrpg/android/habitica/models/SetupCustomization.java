package com.habitrpg.android.habitica.models;


public class SetupCustomization {

    public String key;
    public Integer drawableId;
    public Integer colorId;
    public String text;
    public String path;
    public String category;
    public String subcategory;

    public SetupCustomization() {
        super();
    }

    public String getPath() {
        return path;
    }

    public static SetupCustomization createSize(String key, int drawableId, String text) {
        SetupCustomization customization = new SetupCustomization();
        customization.key = key;
        customization.drawableId = drawableId;
        customization.text = text;
        customization.path = "size";
        customization.category = "body";
        customization.subcategory = "size";
        return customization;
    }

    public static SetupCustomization createShirt(String key, int drawableId) {
        SetupCustomization customization = new SetupCustomization();
        customization.key = key;
        customization.drawableId = drawableId;
        customization.path = "shirt";
        customization.category = "body";
        customization.subcategory = "shirt";
        return customization;
    }

    public static SetupCustomization createSkin(String key, Integer colorId) {
        SetupCustomization customization = new SetupCustomization();
        customization.key = key;
        customization.colorId = colorId;
        customization.path = "skin";
        customization.category = "skin";
        return customization;
    }

    public static SetupCustomization createHairColor(String key, Integer colorId) {
        SetupCustomization customization = new SetupCustomization();
        customization.key = key;
        customization.colorId = colorId;
        customization.path = "hair.color";
        customization.category = "hair";
        customization.subcategory = "color";
        return customization;
    }

    public static SetupCustomization createHairBangs(String key, Integer drawableId) {
        SetupCustomization customization = new SetupCustomization();
        customization.key = key;
        customization.drawableId = drawableId;
        customization.path = "hair.bangs";
        customization.category = "hair";
        customization.subcategory = "bangs";
        return customization;
    }

    public static SetupCustomization createHairPonytail(String key, Integer drawableId) {
        SetupCustomization customization = new SetupCustomization();
        customization.key = key;
        customization.drawableId = drawableId;
        customization.path = "hair.base";
        customization.category = "hair";
        customization.subcategory = "base";
        return customization;
    }

    public static SetupCustomization createGlasses(String key, Integer drawableId) {
        SetupCustomization customization = new SetupCustomization();
        customization.key = key;
        customization.drawableId = drawableId;
        customization.path = "glasses";
        customization.category = "extras";
        customization.subcategory = "glasses";
        return customization;
    }

    public static SetupCustomization createFlower(String key, Integer drawableId) {
        SetupCustomization customization = new SetupCustomization();
        customization.key = key;
        customization.drawableId = drawableId;
        customization.path = "hair.flower";
        customization.category = "extras";
        customization.subcategory = "flower";
        return customization;
    }

    public static SetupCustomization createWheelchair(String key, Integer drawableId) {
        SetupCustomization customization = new SetupCustomization();
        customization.key = key;
        customization.drawableId = drawableId;
        customization.path = "chair";
        customization.category = "extras";
        customization.subcategory = "wheelchair";
        return customization;
    }
}
