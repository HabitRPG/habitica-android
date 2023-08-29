package com.habitrpg.android.habitica.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;

import androidx.annotation.ColorInt;

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

    private static Bitmap imageOfMagicLarge = null;
    public static Bitmap imageOfMagicLarge() {
        if (imageOfMagicLarge != null)
            return imageOfMagicLarge;

        int size = scaleSize(36);
        imageOfMagicLarge = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfMagicLarge);
        HabiticaIcons.drawMagic(canvas, new RectF(0f, 0f, size, size), HabiticaIcons.ResizingBehavior.AspectFit);

        return imageOfMagicLarge;
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

    private static Bitmap imageOfHourglassLarge = null;
    public static Bitmap imageOfHourglassLarge() {
        if (imageOfHourglassLarge != null)
            return imageOfHourglassLarge;

        int size = scaleSize(40);
        imageOfHourglassLarge = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfHourglassLarge);
        HabiticaIcons.drawHourglass(canvas, new RectF(0f, 0f, size, size), HabiticaIcons.ResizingBehavior.AspectFit);

        return imageOfHourglassLarge;
    }

    private static Bitmap imageOfExperienceReward = null;
    public static Bitmap imageOfExperienceReward() {
        if (imageOfExperienceReward != null)
            return imageOfExperienceReward;

        int size = scaleSize(40);
        imageOfExperienceReward = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfExperienceReward);
        HabiticaIcons.drawExperience(canvas, new RectF(0f, 0f, size, size), HabiticaIcons.ResizingBehavior.AspectFit);

        return imageOfExperienceReward;
    }

    private static Bitmap imageOfGoldReward = null;
    public static Bitmap imageOfGoldReward() {
        if (imageOfGoldReward != null)
            return imageOfGoldReward;

        int size = scaleSize(40);
        imageOfGoldReward = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfGoldReward);
        HabiticaIcons.drawGold(canvas, new RectF(0f, 0f, size, size), HabiticaIcons.ResizingBehavior.AspectFit);

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
        HabiticaIcons.drawHeart(canvas, new RectF(0f, 0f, size, size), HabiticaIcons.ResizingBehavior.AspectFit, false);

        return imageOfHeartLarge;
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

        int size = scaleSize(14);
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

        int size = scaleSize(24);
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

        int size = scaleSize(24);
        imageOfUnpinItem = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfUnpinItem);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawAddRemovePin(canvas, false);

        return imageOfUnpinItem;
    }

    public static Bitmap imageOfItemIndicatorNumber(boolean isLimited) {
        int size = scaleSize(28);
        Bitmap imageOfItemIndicatorNumber = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfItemIndicatorNumber);
        canvas.scale(displayDensity, displayDensity);
        int lightColor = Color.parseColor("#A5A1AD");
        int darkColor = Color.parseColor("#525252");
        if (isLimited) {
            lightColor = Color.parseColor("#6133B4");
            darkColor = Color.parseColor("#6133B4");
        }
        HabiticaIcons.drawItemIndicator(canvas, lightColor, Color.parseColor("#C3C0C7"), darkColor, lightColor, false, false, false);

        return imageOfItemIndicatorNumber;
    }

    public static Bitmap imageOfItemIndicatorNumberDark(boolean isLimited) {
        int size = scaleSize(28);
        Bitmap imageOfItemIndicatorNumberDark = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfItemIndicatorNumberDark);
        canvas.scale(displayDensity, displayDensity);
        int lightColor = Color.parseColor("#A5A1AD");
        int darkColor = Color.parseColor("#525252");
        if (isLimited) {
            lightColor = Color.parseColor("#6133B4");
            darkColor = Color.parseColor("#6133B4");
        }
        HabiticaIcons.drawItemIndicator(canvas, lightColor, Color.parseColor("#C3C0C7"), lightColor, darkColor, false, false, true);

        return imageOfItemIndicatorNumberDark;
    }

    public static Bitmap imageOfItemIndicatorLocked(boolean isLimited) {
        int size = scaleSize(28);
        Bitmap imageOfItemIndicatorLocked = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfItemIndicatorLocked);
        canvas.scale(displayDensity, displayDensity);
        int lightColor = Color.parseColor("#EDECEE");
        int darkColor = Color.parseColor("#525252");
        if (isLimited) {
            lightColor = Color.parseColor("#6133B4");
            darkColor = Color.parseColor("#6133B4");
        }
        HabiticaIcons.drawItemIndicator(canvas, lightColor, Color.parseColor("#C3C0C7"), lightColor, darkColor, true, false, false);
        return imageOfItemIndicatorLocked;
    }

    public static Bitmap imageOfItemIndicatorLockedDark(boolean isLimited) {
        int size = scaleSize(28);
        Bitmap imageOfItemIndicatorLockedDark = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfItemIndicatorLockedDark);
        canvas.scale(displayDensity, displayDensity);
        int lightColor = Color.parseColor("#EDECEE");
        int darkColor = Color.parseColor("#525252");
        if (isLimited) {
            lightColor = Color.parseColor("#6133B4");
            darkColor = Color.parseColor("#6133B4");
        }
        HabiticaIcons.drawItemIndicator(canvas, lightColor, Color.parseColor("#C3C0C7"), lightColor, darkColor, true, false, true);
        return imageOfItemIndicatorLockedDark;
    }

    private static Bitmap imageOfItemIndicatorLimited = null;
    public static Bitmap imageOfItemIndicatorLimited() {
        if (imageOfItemIndicatorLimited != null)
            return imageOfItemIndicatorLimited;

        int size = scaleSize(28);
        imageOfItemIndicatorLimited = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfItemIndicatorLimited);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawItemIndicator(canvas, Color.parseColor("#A5A1AD"), Color.parseColor("#C3C0C7"), Color.parseColor("#A5A1AD"), Color.parseColor("#525252"), false, true, false);

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

    private static Bitmap imageOfHourglassShop = null;
    public static Bitmap imageOfHourglassShop() {
        if (imageOfHourglassShop != null)
            return imageOfHourglassShop;

        imageOfHourglassShop = Bitmap.createBitmap(scaleSize(42), scaleSize(53), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfHourglassShop);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawHourglassShop(canvas);

        return imageOfHourglassShop;
    }

    private static Bitmap imageOfAttributeSparklesLeft = null;
    public static Bitmap imageOfAttributeSparklesLeft() {
        if (imageOfAttributeSparklesLeft != null)
            return imageOfAttributeSparklesLeft;

        imageOfAttributeSparklesLeft = Bitmap.createBitmap(scaleSize(77), scaleSize(24), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfAttributeSparklesLeft);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawAttributeSparklesLeft(canvas);

        return imageOfAttributeSparklesLeft;
    }

    private static Bitmap imageOfAttributeSparklesRight = null;
    public static Bitmap imageOfAttributeSparklesRight() {
        if (imageOfAttributeSparklesRight != null)
            return imageOfAttributeSparklesRight;

        imageOfAttributeSparklesRight = Bitmap.createBitmap(scaleSize(77), scaleSize(24), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfAttributeSparklesRight);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawAttributeSparklesRight(canvas);

        return imageOfAttributeSparklesRight;
    }

    private static Bitmap imageOfAttributeAllocateButton = null;
    public static Bitmap imageOfAttributeAllocateButton() {
        if (imageOfAttributeAllocateButton != null)
            return imageOfAttributeAllocateButton;

        imageOfAttributeAllocateButton = Bitmap.createBitmap(scaleSize(24), scaleSize(15), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfAttributeAllocateButton);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawAttributeAllocateButton(canvas);

        return imageOfAttributeAllocateButton;
    }

    private static Bitmap imageOfInfoIcon = null;
    public static Bitmap imageOfInfoIcon(@ColorInt int iconColor) {
        if (imageOfInfoIcon != null)
            return imageOfInfoIcon;

        int size = scaleSize(20);
        imageOfInfoIcon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfInfoIcon);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawInfoIcon(canvas, iconColor);

        return imageOfInfoIcon;
    }

    public static Bitmap imageOfContributorBadge(float contributorTier, boolean isNPC) {
        int size = scaleSize(16);
        Bitmap imageOfContributorBadge = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfContributorBadge);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawContributorBadge(canvas, contributorTier, isNPC);

        return imageOfContributorBadge;
    }

    private static Bitmap imageOfDamage = null;
    public static Bitmap imageOfDamage() {
        if (imageOfDamage != null)
            return imageOfDamage;

        int size = scaleSize(18);
        imageOfDamage = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfDamage);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawDamage(canvas);

        return imageOfDamage;
    }

    public static Bitmap imageOfCaret(int caretColor, boolean pointsUp) {
        int size = scaleSize(16);
        Bitmap imageOfCaret = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfCaret);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawCaret(canvas, caretColor, pointsUp);

        return imageOfCaret;
    }

    private static Bitmap imageOfRageStrikeInactive = null;
    public static Bitmap imageOfRageStrikeInactive() {
        if (imageOfRageStrikeInactive != null)
            return imageOfRageStrikeInactive;

        imageOfRageStrikeInactive = Bitmap.createBitmap(scaleSize(63), scaleSize(82), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfRageStrikeInactive);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawRageStrikeInactive(canvas);

        return imageOfRageStrikeInactive;
    }

    public static Bitmap imageOfRageStrikeActive(Context context, Bitmap rageStrikeNPC) {
        Bitmap imageOfRageStrikeActive = Bitmap.createBitmap(scaleSize(63), scaleSize(82), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfRageStrikeActive);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawRageStrikeActive(canvas, context, rageStrikeNPC);

        return imageOfRageStrikeActive;
    }

    private static Bitmap imageOfRage = null;
    public static Bitmap imageOfRage() {
        if (imageOfRage != null)
            return imageOfRage;

        int size = scaleSize(18);
        imageOfRage = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfRage);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawRage(canvas);

        return imageOfRage;
    }

    public static Bitmap imageOfLocked(@ColorInt int lockColor) {
        return imageOfLocked(lockColor, 15, 17);
    }

    public static Bitmap imageOfLocked(@ColorInt int lockColor, int width, int height) {
        Bitmap imageOfLocked = Bitmap.createBitmap(scaleSize(width), scaleSize(height), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfLocked);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawLocked(canvas, new RectF(0f, 0f, width, height), HabiticaIcons.ResizingBehavior.AspectFit, lockColor);
        return imageOfLocked;
    }

    private static Bitmap imageOfParticipantsIcon = null;
    public static Bitmap imageOfParticipantsIcon() {
        if (imageOfParticipantsIcon != null)
            return imageOfParticipantsIcon;

        int size = scaleSize(20);
        imageOfParticipantsIcon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfParticipantsIcon);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawParticipantsIcon(canvas);

        return imageOfParticipantsIcon;
    }

    private static Bitmap imageOfChatReplyIcon = null;
    public static Bitmap imageOfChatReplyIcon() {
        if (imageOfChatReplyIcon != null)
            return imageOfChatReplyIcon;

        int size = scaleSize(17);
        imageOfChatReplyIcon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfChatReplyIcon);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawChatReplyIcon(canvas);

        return imageOfChatReplyIcon;
    }

    private static Bitmap imageOfChatCopyIcon = null;
    public static Bitmap imageOfChatCopyIcon() {
        if (imageOfChatCopyIcon != null)
            return imageOfChatCopyIcon;

        int size = scaleSize(17);
        imageOfChatCopyIcon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfChatCopyIcon);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawChatCopyIcon(canvas);

        return imageOfChatCopyIcon;
    }

    private static Bitmap imageOfChatReportIcon = null;
    public static Bitmap imageOfChatReportIcon() {
        if (imageOfChatReportIcon != null)
            return imageOfChatReportIcon;

        int size = scaleSize(17);
        imageOfChatReportIcon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfChatReportIcon);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawChatReportIcon(canvas);

        return imageOfChatReportIcon;
    }

    private static Bitmap imageOfChatDeleteIcon = null;
    public static Bitmap imageOfChatDeleteIcon() {
        if (imageOfChatDeleteIcon != null)
            return imageOfChatDeleteIcon;

        int size = scaleSize(17);
        imageOfChatDeleteIcon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfChatDeleteIcon);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawChatDeleteIcon(canvas);

        return imageOfChatDeleteIcon;
    }

    private static Bitmap imageOfTwoHandedIcon = null;
    public static Bitmap imageOfTwoHandedIcon() {
        if (imageOfTwoHandedIcon != null)
            return imageOfTwoHandedIcon;

        int size = scaleSize(15);
        imageOfTwoHandedIcon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfTwoHandedIcon);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawTwoHandedIcon(canvas);

        return imageOfTwoHandedIcon;
    }

    public static Bitmap imageOfCheckmark(int checkmarkColor, float percentage) {
        Bitmap imageOfCheckmark = Bitmap.createBitmap(scaleSize(16), scaleSize(12), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfCheckmark);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawCheckmark(canvas, checkmarkColor, percentage);

        return imageOfCheckmark;
    }

    private static Bitmap imageOfAlertIcon = null;
    public static Bitmap imageOfAlertIcon() {
        if (imageOfAlertIcon != null)
            return imageOfAlertIcon;

        int size = scaleSize(16);
        imageOfAlertIcon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfAlertIcon);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawAlertIcon(canvas);

        return imageOfAlertIcon;
    }

    private static Bitmap imageOfBuffIcon = null;
    public static Bitmap imageOfBuffIcon() {
        if (imageOfBuffIcon != null)
            return imageOfBuffIcon;

        int size = scaleSize(15);
        imageOfBuffIcon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfBuffIcon);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawBuffIcon(canvas);

        return imageOfBuffIcon;
    }

    public static Bitmap imageOfTaskDifficultyStars(int taskTintColor, float difficulty, boolean isActive) {
        int size = scaleSize(36);
        Bitmap imageOfTaskDifficultyStars = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfTaskDifficultyStars);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawTaskDifficultyStars(canvas, taskTintColor, difficulty, isActive);

        return imageOfTaskDifficultyStars;
    }

    public static Bitmap imageOfStats() {
        int width = scaleSize(30);
        int height = scaleSize(30);
        Bitmap imageOfHabitControlMinus = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfHabitControlMinus);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawStats(canvas);

        return imageOfHabitControlMinus;
    }

    public static Bitmap imageOfGuildCrest(boolean isOwner, boolean isPublic, float memberCount, String memberCountLabel) {
        Bitmap imageOfGuildCrest = Bitmap.createBitmap(scaleSize(40), scaleSize(38), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfGuildCrest);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawGuildCrest(canvas, isOwner, isPublic, memberCount, memberCountLabel);

        return imageOfGuildCrest;
    }

    public static Bitmap imageOfGuildCrestMedium(float memberCount) {
        Bitmap imageOfGuildCrestMedium = Bitmap.createBitmap(scaleSize(30), scaleSize(34), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfGuildCrestMedium);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawGuildCrestMedium(canvas, memberCount);

        return imageOfGuildCrestMedium;
    }

    public static Bitmap imageOfGuildCrestSmall(float memberCount) {
        Bitmap imageOfGuildCrestSmall = Bitmap.createBitmap(scaleSize(16), scaleSize(16), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageOfGuildCrestSmall);
        canvas.scale(displayDensity, displayDensity);
        HabiticaIcons.drawGuildCrestSmall(canvas, memberCount);

        return imageOfGuildCrestSmall;
    }
}
