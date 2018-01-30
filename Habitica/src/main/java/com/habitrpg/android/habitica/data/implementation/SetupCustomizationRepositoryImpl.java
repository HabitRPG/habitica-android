package com.habitrpg.android.habitica.data.implementation;

import android.content.Context;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.SetupCustomizationRepository;
import com.habitrpg.android.habitica.models.SetupCustomization;
import com.habitrpg.android.habitica.models.user.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;


public class SetupCustomizationRepositoryImpl implements SetupCustomizationRepository {

    private final Context context;

    @Inject
    public SetupCustomizationRepositoryImpl(Context context) {
        this.context = context;
    }

    @Override
    public List<SetupCustomization> getCustomizations(String category, User user) {
        return getCustomizations(category, null, user);
    }

    @Override
    public List<SetupCustomization> getCustomizations(String category, String subcategory, User user) {
        switch (category) {
            case "body": {
                switch (subcategory) {
                    case "size":
                        return getSizes();
                    case "shirt":
                        return getShirts(user.getPreferences().getSize());
                }
            }
            case "skin":
                return getSkins();
            case "hair": {
                switch (subcategory) {
                    case "bangs":
                        return getBangs(user.getPreferences().getHair().getColor());
                    case "ponytail":
                        return getHairBases(user.getPreferences().getHair().getColor());
                    case "color":
                        return getHairColors();
                }
            }
            case "extras": {
                switch (subcategory) {
                    case "flower":
                        return getFlowers();
                    case "glasses":
                        return getGlasses();
                    case "wheelchair":
                        return getWheelchairs();
                }
            }
        }
        return new ArrayList<>();
    }

    private List<SetupCustomization> getWheelchairs() {
        return Arrays.asList(
                SetupCustomization.Companion.createWheelchair("none", 0),
                SetupCustomization.Companion.createWheelchair("black", R.drawable.creator_chair_black),
                SetupCustomization.Companion.createWheelchair("blue", R.drawable.creator_chair_blue),
                SetupCustomization.Companion.createWheelchair("green", R.drawable.creator_chair_green),
                SetupCustomization.Companion.createWheelchair("pink", R.drawable.creator_chair_pink),
                SetupCustomization.Companion.createWheelchair("red", R.drawable.creator_chair_red),
                SetupCustomization.Companion.createWheelchair("yellow", R.drawable.creator_chair_yellow)
        );
    }

    private List<SetupCustomization> getGlasses() {
        return Arrays.asList(
                SetupCustomization.Companion.createGlasses("", R.drawable.creator_blank_face),
                SetupCustomization.Companion.createGlasses("eyewear_special_blackTopFrame", R.drawable.creator_eyewear_special_blacktopframe),
                SetupCustomization.Companion.createGlasses("eyewear_special_blueTopFrame", R.drawable.creator_eyewear_special_bluetopframe),
                SetupCustomization.Companion.createGlasses("eyewear_special_greenTopFrame", R.drawable.creator_eyewear_special_greentopframe),
                SetupCustomization.Companion.createGlasses("eyewear_special_pinkTopFrame", R.drawable.creator_eyewear_special_pinktopframe),
                SetupCustomization.Companion.createGlasses("eyewear_special_redTopFrame", R.drawable.creator_eyewear_special_redtopframe),
                SetupCustomization.Companion.createGlasses("eyewear_special_yellowTopFrame", R.drawable.creator_eyewear_special_yellowtopframe),
                SetupCustomization.Companion.createGlasses("eyewear_special_whiteTopFrame", R.drawable.creator_eyewear_special_whitetopframe)
        );
    }

    private List<SetupCustomization> getFlowers() {
        return Arrays.asList(
                SetupCustomization.Companion.createFlower("0", R.drawable.creator_blank_face),
                SetupCustomization.Companion.createFlower("1", R.drawable.creator_hair_flower_1),
                SetupCustomization.Companion.createFlower("2", R.drawable.creator_hair_flower_2),
                SetupCustomization.Companion.createFlower("3", R.drawable.creator_hair_flower_3),
                SetupCustomization.Companion.createFlower("4", R.drawable.creator_hair_flower_4),
                SetupCustomization.Companion.createFlower("5", R.drawable.creator_hair_flower_5),
                SetupCustomization.Companion.createFlower("6", R.drawable.creator_hair_flower_6)
        );
    }

    private List<SetupCustomization> getHairColors() {
        return Arrays.asList(
                SetupCustomization.Companion.createHairColor("white", R.color.hair_white),
                SetupCustomization.Companion.createHairColor("brown", R.color.hair_brown),
                SetupCustomization.Companion.createHairColor("blond", R.color.hair_blond),
                SetupCustomization.Companion.createHairColor("red", R.color.hair_red),
                SetupCustomization.Companion.createHairColor("black", R.color.hair_black)
        );
    }

    private List<SetupCustomization> getHairBases(String color) {
        return Arrays.asList(
                SetupCustomization.Companion.createHairPonytail("0", R.drawable.creator_blank_face),
                SetupCustomization.Companion.createHairPonytail("1", getResId("creator_hair_base_1_"+color)),
                SetupCustomization.Companion.createHairPonytail("3", getResId("creator_hair_base_3_"+color))
                );
    }

    private List<SetupCustomization> getBangs(String color) {
        return Arrays.asList(
                SetupCustomization.Companion.createHairBangs("0", R.drawable.creator_blank_face),
                SetupCustomization.Companion.createHairBangs("1", getResId("creator_hair_bangs_1_"+color)),
                SetupCustomization.Companion.createHairBangs("2", getResId("creator_hair_bangs_2_"+color)),
                SetupCustomization.Companion.createHairBangs("3", getResId("creator_hair_bangs_3_"+color))
        );
    }

    private List<SetupCustomization> getSizes() {
        return Arrays.asList(
                SetupCustomization.Companion.createSize("slim", R.drawable.creator_slim_shirt_black, context.getString(R.string.avatar_size_slim)),
                SetupCustomization.Companion.createSize("broad", R.drawable.creator_broad_shirt_black, context.getString(R.string.avatar_size_broad))
        );
    }

    private List<SetupCustomization> getShirts(String size) {
        if (size.equals("broad")) {
            return Arrays.asList(
                    SetupCustomization.Companion.createShirt("black", R.drawable.creator_broad_shirt_black),
                    SetupCustomization.Companion.createShirt("blue", R.drawable.creator_broad_shirt_blue),
                    SetupCustomization.Companion.createShirt("green", R.drawable.creator_broad_shirt_green),
                    SetupCustomization.Companion.createShirt("pink", R.drawable.creator_broad_shirt_pink),
                    SetupCustomization.Companion.createShirt("white", R.drawable.creator_broad_shirt_white),
                    SetupCustomization.Companion.createShirt("yellow", R.drawable.creator_broad_shirt_yellow)
            );
        } else {
            return Arrays.asList(
                    SetupCustomization.Companion.createShirt("black", R.drawable.creator_slim_shirt_black),
                    SetupCustomization.Companion.createShirt("blue", R.drawable.creator_slim_shirt_blue),
                    SetupCustomization.Companion.createShirt("green", R.drawable.creator_slim_shirt_green),
                    SetupCustomization.Companion.createShirt("pink", R.drawable.creator_slim_shirt_pink),
                    SetupCustomization.Companion.createShirt("white", R.drawable.creator_slim_shirt_white),
                    SetupCustomization.Companion.createShirt("yellow", R.drawable.creator_slim_shirt_yellow)
            );
        }
    }

    private List<SetupCustomization> getSkins() {
        return Arrays.asList(
                SetupCustomization.Companion.createSkin("ddc994", R.color.skin_ddc994),
                SetupCustomization.Companion.createSkin("f5a76e", R.color.skin_f5a76e),
                SetupCustomization.Companion.createSkin("ea8349", R.color.skin_ea8349),
                SetupCustomization.Companion.createSkin("c06534", R.color.skin_c06534),
                SetupCustomization.Companion.createSkin("98461a", R.color.skin_98461a),
                SetupCustomization.Companion.createSkin("915533", R.color.skin_915533),
                SetupCustomization.Companion.createSkin("c3e1dc", R.color.skin_c3e1dc),
                SetupCustomization.Companion.createSkin("6bd049", R.color.skin_6bd049)
        );
    }

    private int getResId(String resName) {

        try {
            return context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
