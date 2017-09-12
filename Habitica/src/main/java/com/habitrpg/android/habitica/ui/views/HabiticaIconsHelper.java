package com.habitrpg.android.habitica.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by phillip on 05.09.17.
 */

public class HabiticaIconsHelper {

    private static float displayDensity = 1.0f;

    public static void init(Context context) {
        displayDensity = context.getResources().getDisplayMetrics().density;
    }

    private static int scaleSize(int size) {
        return (int) (size * displayDensity);
    }

    private static Bitmap imageOfExperience = null;
    public static Bitmap imageOfExperience() {
        if (imageOfExperience != null)
            return imageOfExperience;

        int size = scaleSize(18);
        imageOfExperience = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfExperience);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawExperience(canvas);

        return imageOfExperience;
    }

    private static Bitmap imageOfMagic = null;
    public static Bitmap imageOfMagic() {
        if (imageOfMagic != null)
            return imageOfMagic;

        int size = scaleSize(18);
        imageOfMagic = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfMagic);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawMagic(canvas);

        return imageOfMagic;
    }

    private static Bitmap imageOfGold = null;
    public static Bitmap imageOfGold() {
        if (imageOfGold != null)
            return imageOfGold;

        int size = scaleSize(18);
        imageOfGold = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfGold);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawGold(canvas);

        return imageOfGold;
    }

    private static Bitmap imageOfGem = null;
    public static Bitmap imageOfGem() {
        if (imageOfGem != null)
            return imageOfGem;

        int size = scaleSize(18);
        imageOfGem = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfGem);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawGem(canvas);

        return imageOfGem;
    }

    private static Bitmap imageOfHourglass = null;
    public static Bitmap imageOfHourglass() {
        if (imageOfHourglass != null)
            return imageOfHourglass;

        int size = scaleSize(18);
        imageOfHourglass = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfHourglass);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawHourglass(canvas);

        return imageOfHourglass;
    }

    private static Bitmap imageOfExperienceReward = null;
    public static Bitmap imageOfExperienceReward() {
        if (imageOfExperienceReward != null)
            return imageOfExperienceReward;

        int size = scaleSize(40);
        imageOfExperienceReward = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfExperienceReward);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawExperience(canvas);

        return imageOfExperienceReward;
    }

    private static Bitmap imageOfGoldReward = null;
    public static Bitmap imageOfGoldReward() {
        if (imageOfGoldReward != null)
            return imageOfGoldReward;

        int size = scaleSize(40);
        imageOfGoldReward = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfGoldReward);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawGold(canvas);

        return imageOfGoldReward;
    }

    private static Bitmap imageOfHeartDarkBg = null;
    public static Bitmap imageOfHeartDarkBg() {
        if (imageOfHeartDarkBg != null)
            return imageOfHeartDarkBg;

        int size = scaleSize(18);
        imageOfHeartDarkBg = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfHeartDarkBg);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawHeart(canvas, true);

        return imageOfHeartDarkBg;
    }

    private static Bitmap imageOfHeartLightBg = null;
    public static Bitmap imageOfHeartLightBg() {
        if (imageOfHeartLightBg != null)
            return imageOfHeartLightBg;

        int size = scaleSize(18);
        imageOfHeartLightBg = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfHeartLightBg);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawHeart(canvas, false);

        return imageOfHeartLightBg;
    }

    private static Bitmap imageOfHeartLarge = null;
    public static Bitmap imageOfHeartLarge() {
        if (imageOfHeartLarge != null)
            return imageOfHeartLarge;

        int size = scaleSize(36);
        imageOfHeartLarge = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfHeartLarge);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawHeart(canvas, false);

        return imageOfHeartLarge;
    }

    public static Bitmap imageOfDifficultyStars(float difficulty) {
        Bitmap imageOfDifficultyStars = Bitmap.createBitmap(scaleSize(48), scaleSize(12), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfDifficultyStars);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawDifficultyStars(canvas, difficulty);

        return imageOfDifficultyStars;
    }

    private static Bitmap imageOfStarSmall = null;
    public static Bitmap imageOfStarSmall() {
        if (imageOfStarSmall != null)
            return imageOfStarSmall;

        int size = scaleSize(9);
        imageOfStarSmall = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfStarSmall);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawStarSmall(canvas);

        return imageOfStarSmall;
    }

    private static Bitmap imageOfStarLarge = null;
    public static Bitmap imageOfStarLarge() {
        if (imageOfStarLarge != null)
            return imageOfStarLarge;

        int size = scaleSize(27);
        imageOfStarLarge = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfStarLarge);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawStarLarge(canvas);

        return imageOfStarLarge;
    }

    private static Bitmap imageOfStarMedium = null;
    public static Bitmap imageOfStarMedium() {
        if (imageOfStarMedium != null)
            return imageOfStarMedium;

        int size = scaleSize(21);
        imageOfStarMedium = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfStarMedium);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawStarMedium(canvas);

        return imageOfStarMedium;
    }

    private static Bitmap imageOfPinnedItem = null;
    public static Bitmap imageOfPinnedItem() {
        if (imageOfPinnedItem != null)
            return imageOfPinnedItem;

        int size = scaleSize(16);
        imageOfPinnedItem = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfPinnedItem);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawPinnedItem(canvas);

        return imageOfPinnedItem;
    }

    private static Bitmap imageOfPinItem = null;
    public static Bitmap imageOfPinItem() {
        if (imageOfPinItem != null)
            return imageOfPinItem;

        int size = scaleSize(40);
        imageOfPinItem = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfPinItem);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawAddRemovePin(canvas, true);

        return imageOfPinItem;
    }

    private static Bitmap imageOfUnpinItem = null;
    public static Bitmap imageOfUnpinItem() {
        if (imageOfUnpinItem != null)
            return imageOfUnpinItem;

        int size = scaleSize(40);
        imageOfUnpinItem = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfUnpinItem);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawAddRemovePin(canvas, false);

        return imageOfUnpinItem;
    }

    private static Bitmap imageOfItemIndicatorNumber = null;
    public static Bitmap imageOfItemIndicatorNumber() {
        if (imageOfItemIndicatorNumber != null)
            return imageOfItemIndicatorNumber;

        int size = scaleSize(28);
        imageOfItemIndicatorNumber = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfItemIndicatorNumber);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawItemIndicator(canvas, false, false);

        return imageOfItemIndicatorNumber;
    }

    private static Bitmap imageOfItemIndicatorLocked = null;
    public static Bitmap imageOfItemIndicatorLocked() {
        if (imageOfItemIndicatorLocked != null)
            return imageOfItemIndicatorLocked;

        int size = scaleSize(28);
        imageOfItemIndicatorLocked = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfItemIndicatorLocked);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawItemIndicator(canvas, true, false);

        return imageOfItemIndicatorLocked;
    }

    private static Bitmap imageOfItemIndicatorLimited = null;
    public static Bitmap imageOfItemIndicatorLimited() {
        if (imageOfItemIndicatorLimited != null)
            return imageOfItemIndicatorLimited;

        int size = scaleSize(28);
        imageOfItemIndicatorLimited = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfItemIndicatorLimited);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawItemIndicator(canvas, false, true);

        return imageOfItemIndicatorLimited;
    }

    private static Bitmap imageOfGem_36 = null;
    public static Bitmap imageOfGem_36() {
        if (imageOfGem_36 != null)
            return imageOfGem_36;

        int size = scaleSize(36);
        imageOfGem_36 = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfGem_36);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawGem(canvas);

        return imageOfGem_36;
    }

    private static Bitmap imageOfWarriorLightBg = null;
    public static Bitmap imageOfWarriorLightBg() {
        if (imageOfWarriorLightBg != null)
            return imageOfWarriorLightBg;

        int size = scaleSize(32);
        imageOfWarriorLightBg = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfWarriorLightBg);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawWarriorLightBg(canvas);

        return imageOfWarriorLightBg;
    }

    private static Bitmap imageOfWarriorDarkBg = null;
    public static Bitmap imageOfWarriorDarkBg() {
        if (imageOfWarriorDarkBg != null)
            return imageOfWarriorDarkBg;

        int size = scaleSize(32);
        imageOfWarriorDarkBg = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfWarriorDarkBg);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawWarriorDarkBg(canvas);

        return imageOfWarriorDarkBg;
    }

    private static Bitmap imageOfRogueLightBg = null;
    public static Bitmap imageOfRogueLightBg() {
        if (imageOfRogueLightBg != null)
            return imageOfRogueLightBg;

        int size = scaleSize(32);
        imageOfRogueLightBg = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfRogueLightBg);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawRogueLightBg(canvas);

        return imageOfRogueLightBg;
    }

    private static Bitmap imageOfRogueDarkBg = null;
    public static Bitmap imageOfRogueDarkBg() {
        if (imageOfRogueDarkBg != null)
            return imageOfRogueDarkBg;

        int size = scaleSize(32);
        imageOfRogueDarkBg = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfRogueDarkBg);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawRogueDarkBg(canvas);

        return imageOfRogueDarkBg;
    }

    private static Bitmap imageOfHealerLightBg = null;
    public static Bitmap imageOfHealerLightBg() {
        if (imageOfHealerLightBg != null)
            return imageOfHealerLightBg;

        int size = scaleSize(32);
        imageOfHealerLightBg = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfHealerLightBg);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawHealerLightBg(canvas);

        return imageOfHealerLightBg;
    }

    private static Bitmap imageOfHealerDarkBg = null;
    public static Bitmap imageOfHealerDarkBg() {
        if (imageOfHealerDarkBg != null)
            return imageOfHealerDarkBg;

        int size = scaleSize(32);
        imageOfHealerDarkBg = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfHealerDarkBg);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawHealerDarkBg(canvas);

        return imageOfHealerDarkBg;
    }

    private static Bitmap imageOfMageDarkBg = null;
    public static Bitmap imageOfMageDarkBg() {
        if (imageOfMageDarkBg != null)
            return imageOfMageDarkBg;

        int size = scaleSize(32);
        imageOfMageDarkBg = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfMageDarkBg);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawMageDarkBg(canvas);

        return imageOfMageDarkBg;
    }

    private static Bitmap imageOfMageLightBg = null;
    public static Bitmap imageOfMageLightBg() {
        if (imageOfMageLightBg != null)
            return imageOfMageLightBg;

        int size = scaleSize(32);
        imageOfMageLightBg = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfMageLightBg);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawMageLightBg(canvas);

        return imageOfMageLightBg;
    }
}
