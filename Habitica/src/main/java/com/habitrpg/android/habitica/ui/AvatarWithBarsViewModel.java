package com.habitrpg.android.habitica.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.BoughtGemsEvent;
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand;
import com.habitrpg.android.habitica.events.commands.OpenMenuItemCommand;
import com.habitrpg.android.habitica.models.Avatar;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.ui.menu.MainDrawerBuilder;
import com.habitrpg.android.habitica.ui.views.CurrencyViews;
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper;
import com.habitrpg.android.habitica.ui.views.ValueBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AvatarWithBarsViewModel {
    @BindView(R.id.hpBar)
    ValueBar hpBar;
    @BindView(R.id.xpBar)
    ValueBar xpBar;
    @BindView(R.id.mpBar)
    ValueBar mpBar;
    @BindView(R.id.avatarView)
    AvatarView avatarView;

    private Context context;

    @BindView(R.id.lvl_tv)
    TextView lvlText;
    @BindView(R.id.currencyView)
    CurrencyViews currencyView;

    private Avatar userObject;

    private int cachedMaxHealth, cachedMaxExp, cachedMaxMana;

    public AvatarWithBarsViewModel(Context context, View v) {
        this.context = context;

        if (v == null) {
            Log.w("AvatarWithBarsViewModel", "View is null");
            return;
        }

        ButterKnife.bind(this, v);

        hpBar.setIcon(HabiticaIconsHelper.imageOfHeartLightBg());
        xpBar.setIcon(HabiticaIconsHelper.imageOfExperience());
        mpBar.setIcon(HabiticaIconsHelper.imageOfMagic());

        setHpBarData(0, 50);
        setXpBarData(0, 1);
        setMpBarData(0, 1);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void updateData(Avatar user) {
        userObject = user;

        Stats stats = user.getStats();

        String userClass = "";

        avatarView.setAvatar(user);

        if (stats.getHabitClass() != null) {
            userClass = stats.getTranslatedClassName(context);
        }

        mpBar.setVisibility((stats.getHabitClass() == null || stats.getLvl() < 10 || user.getPreferences().getDisableClasses()) ? View.GONE : View.VISIBLE);

        if (!user.hasClass()) {
            lvlText.setText(context.getString(R.string.user_level, user.getStats().getLvl()));
            lvlText.setCompoundDrawables(null, null, null, null);
        } else {
            lvlText.setText(context.getString(R.string.user_level_with_class, user.getStats().getLvl(), userClass.substring(0, 1).toUpperCase(Locale.getDefault()) + userClass.substring(1)));
            Drawable drawable = null;
            switch (stats.getHabitClass()) {
                case "warrior":
                    drawable = new BitmapDrawable(context.getResources(), HabiticaIconsHelper.imageOfWarriorDarkBg());
                    break;
                case "rogue":
                    drawable = new BitmapDrawable(context.getResources(), HabiticaIconsHelper.imageOfRogueDarkBg());
                    break;
                case "wizard":
                    drawable = new BitmapDrawable(context.getResources(), HabiticaIconsHelper.imageOfMageDarkBg());
                    break;
                case "healer":
                    drawable = new BitmapDrawable(context.getResources(), HabiticaIconsHelper.imageOfHealerDarkBg());
                    break;
            }
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                        drawable.getMinimumHeight());
            }
            lvlText.setCompoundDrawables(drawable, null, null, null);
        }

        setHpBarData(stats.getHp().floatValue(), stats.getMaxHealth());
        setXpBarData(stats.getExp().floatValue(), stats.getToNextLevel());
        setMpBarData(stats.getMp().floatValue(), stats.getMaxMP());

        currencyView.setHourglasses(user.getHourglassCount());
        currencyView.setGold(stats.getGp());
        currencyView.setGems(user.getGemCount());
    }

    private void setHpBarData(float value, int valueMax) {
        if (valueMax == 0) {
            valueMax = cachedMaxHealth;
        } else {
            cachedMaxHealth = valueMax;
        }
        hpBar.set(Math.ceil(value), valueMax);
    }

    private void setXpBarData(float value, int valueMax) {
        if (valueMax == 0) {
            valueMax = cachedMaxExp;
        } else {
            cachedMaxExp = valueMax;
        }
        xpBar.set(Math.floor(value), valueMax);
    }

    private void setMpBarData(float value, int valueMax) {
        if (valueMax == 0) {
            valueMax = cachedMaxMana;
        } else {
            cachedMaxMana = valueMax;
        }
        mpBar.set(Math.floor(value), valueMax);
    }

    @Subscribe
    public void onEvent(BoughtGemsEvent gemsEvent) {
        Integer gems = userObject.getGemCount();
        gems += gemsEvent.NewGemsToAdd;
        currencyView.setGems(gems);
    }

    @OnClick(R.id.currencyView)
    public void gemTextClicked() {
        EventBus.getDefault().post(new OpenGemPurchaseFragmentCommand());
    }

    @OnClick(R.id.avatarView)
    public void avatarViewClicked() {
        OpenMenuItemCommand event = new OpenMenuItemCommand();
        event.identifier = MainDrawerBuilder.INSTANCE.getSIDEBAR_AVATAR();
        EventBus.getDefault().post(event);
    }

    public void valueBarLabelsToBlack() {
        hpBar.setLightBackground(true);
        xpBar.setLightBackground(true);
        mpBar.setLightBackground(true);
    }

    public static void setHpBarData(ValueBar valueBar, Stats stats) {
        Integer maxHP = stats.getMaxHealth();
        if (maxHP == null || maxHP == 0) {
            maxHP = 50;
        }

        valueBar.set((float) Math.ceil(stats.getHp().floatValue()), maxHP);
    }
}
