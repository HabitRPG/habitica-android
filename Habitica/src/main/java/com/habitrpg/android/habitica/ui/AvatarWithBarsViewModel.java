package com.habitrpg.android.habitica.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.BoughtGemsEvent;
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand;
import com.habitrpg.android.habitica.events.commands.OpenMenuItemCommand;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.menu.MainDrawerBuilder;
import com.habitrpg.android.habitica.ui.views.ValueBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AvatarWithBarsViewModel implements View.OnClickListener {
    @BindView(R.id.hpBar)
    ValueBar hpBar;
    @BindView(R.id.xpBar)
    ValueBar xpBar;
    @BindView(R.id.mpBar)
    ValueBar mpBar;
    @BindView(R.id.avatarView)
    AvatarView avatarView;

    private android.content.res.Resources res;

    private Context context;

    @BindView(R.id.lvl_tv)
    TextView lvlText;
    @BindView(R.id.gold_tv)
    TextView goldText;
    @BindView(R.id.silver_tv)
    TextView silverText;
    @BindView(R.id.gems_tv)
    TextView gemsText;

    private User userObject;

    private int cachedMaxHealth, cachedMaxExp, cachedMaxMana;

    public AvatarWithBarsViewModel(Context context, View v) {
        this.context = context;
        res = context.getResources();

        if (v == null) {
            Log.w("AvatarWithBarsViewModel", "View is null");
            return;
        }

        ButterKnife.bind(this, v);


        setHpBarData(0, 50);
        setXpBarData(0, 1);
        setMpBarData(0, 1);

        gemsText.setClickable(true);
        gemsText.setOnClickListener(this);

        avatarView.setClickable(true);
        avatarView.setOnClickListener(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void updateData(User user) {
        userObject = user;

        Stats stats = user.getStats();

        String userClass = "";
        int gp = (stats.getGp().intValue());
        int sp = (int) ((stats.getGp() - gp) * 100);

        avatarView.setUser(user);

        if (stats.getHabitClass() != null) {
            userClass = stats.getTranslatedClassName(context);
        }

        mpBar.setVisibility((stats.getHabitClass() == null || stats.getLvl() < 10 || user.getPreferences().getDisableClasses()) ? View.GONE : View.VISIBLE);

        if (user.getPreferences() != null && user.getFlags() != null && (user.getPreferences().getDisableClasses() || !user.getFlags().getClassSelected() || userClass.length() == 0)) {
            lvlText.setText(context.getString(R.string.user_level, user.getStats().getLvl()));
            lvlText.setCompoundDrawables(null, null, null, null);
        } else {
            lvlText.setText(context.getString(R.string.user_level_with_class, user.getStats().getLvl(), userClass.substring(0, 1).toUpperCase(Locale.getDefault()) + userClass.substring(1)));
            Drawable drawable;
            switch (stats.getHabitClass()) {
                case "warrior":
                    drawable = ResourcesCompat.getDrawable(res, R.drawable.ic_header_warrior, null);
                    break;
                case "rogue":
                    drawable = ResourcesCompat.getDrawable(res, R.drawable.ic_header_rogue, null);
                    break;
                case "wizard":
                    drawable = ResourcesCompat.getDrawable(res, R.drawable.ic_header_mage, null);
                    break;
                case "healer":
                    drawable = ResourcesCompat.getDrawable(res, R.drawable.ic_header_healer, null);
                    break;
                default:
                    drawable = ResourcesCompat.getDrawable(res, R.drawable.ic_header_warrior, null);

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

        goldText.setText(String.valueOf(gp));
        silverText.setText(String.valueOf(sp));

        Double gems = user.getBalance() * 4;
        gemsText.setText(String.valueOf(gems.intValue()));
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
        Double gems = userObject.getBalance() * 4;
        gems += gemsEvent.NewGemsToAdd;
        gemsText.setText(String.valueOf(gems.intValue()));
    }

    @Override
    public void onClick(View view) {
        if (view == gemsText) {
            // Gems Clicked

            EventBus.getDefault().post(new OpenGemPurchaseFragmentCommand());
        } else {
            // Avatar overview
            OpenMenuItemCommand event = new OpenMenuItemCommand();
            event.identifier = MainDrawerBuilder.SIDEBAR_AVATAR;
            EventBus.getDefault().post(event);
        }
    }

    public void hideGems() {
        gemsText.setVisibility(View.GONE);
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
