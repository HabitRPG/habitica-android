package com.habitrpg.android.habitica.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.ValueBarBinding;
import com.habitrpg.android.habitica.events.BoughtGemsEvent;
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand;
import com.habitrpg.android.habitica.events.commands.OpenMenuItemCommand;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.ui.menu.MainDrawerBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

/**
 * Created by Negue on 14.06.2015.
 */
public class AvatarWithBarsViewModel implements View.OnClickListener {
    private ValueBarBinding hpBar;
    private ValueBarBinding xpBar;
    private ValueBarBinding mpBar;

    private AvatarView avatarView;

    private android.content.res.Resources res;

    private Context context;

    private TextView lvlText, goldText, silverText, gemsText;
    private User userObject;

    private int cachedMaxHealth, cachedMaxExp, cachedMaxMana;

    public AvatarWithBarsViewModel(Context context, View v) {
        this.context = context;

        res = context.getResources();

        if (v == null) {
            Log.w("AvatarWithBarsViewModel", "View is null");
            return;
        }

        lvlText = (TextView) v.findViewById(R.id.lvl_tv);
        goldText = (TextView) v.findViewById(R.id.gold_tv);
        silverText = (TextView) v.findViewById(R.id.silver_tv);
        gemsText = (TextView) v.findViewById(R.id.gems_tv);
        View hpBarView = v.findViewById(R.id.hpBar);

        avatarView = (AvatarView) v.findViewById(R.id.avatarView);
        hpBar = DataBindingUtil.bind(hpBarView);
        xpBar = DataBindingUtil.bind(v.findViewById(R.id.xpBar));
        mpBar = DataBindingUtil.bind(v.findViewById(R.id.mpBar));

        setHpBarData(0, 50);
        setXpBarData(0, 1);
        setMpBarData(0, 1);

        gemsText.setClickable(true);
        gemsText.setOnClickListener(this);

        avatarView.setClickable(true);
        avatarView.setOnClickListener(this);
    }

    public static void setHpBarData(ValueBarBinding valueBar, Stats stats, Context ctx) {
        Integer maxHP = stats.getMaxHealth();
        if (maxHP == null || maxHP == 0) {
            maxHP = 50;
        }

        setValueBar(valueBar, (float) Math.ceil(stats.getHp().floatValue()), maxHP, ctx.getString(R.string.HP_default), ContextCompat.getColor(ctx, R.color.hpColor), R.drawable.ic_header_heart);
    }

    // Layout_Weight don't accepts 0.7/0.3 to have 70% filled instead it shows the 30% , so I had to switch the values
    // but on a 1.0/0.0 which switches to 0.0/1.0 it shows the blank part full size...
    private static void setValueBar(ValueBarBinding valueBar, float value, float valueMax, String description, int color, int icon) {
        double percent = Math.min(1, value / valueMax);

        valueBar.setWeightToShow((float) percent);
        valueBar.setText((int) value + "/" + (int) valueMax);
        valueBar.setDescription(description);
        valueBar.setBarForegroundColor(color);
        valueBar.icHeader.setImageResource(icon);
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

        mpBar.valueBarLayout.setVisibility((stats.getHabitClass() == null || stats.getLvl() < 10 || user.getPreferences().getDisableClasses()) ? View.GONE : View.VISIBLE);

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

    public void setHpBarData(float value, int valueMax) {
        if (valueMax == 0) {
            valueMax = cachedMaxHealth;
        } else {
            cachedMaxHealth = valueMax;
        }
        setValueBar(hpBar, (float) Math.ceil(value), valueMax, context.getString(R.string.HP_default), ContextCompat.getColor(context, R.color.hpColor), R.drawable.ic_header_heart);
    }

    public void setXpBarData(float value, int valueMax) {
        if (valueMax == 0) {
            valueMax = cachedMaxExp;
        } else {
            cachedMaxExp = valueMax;
        }
        setValueBar(xpBar, (float) Math.floor(value), valueMax, context.getString(R.string.XP_default), ContextCompat.getColor(context, R.color.xpColor), R.drawable.ic_header_exp);
    }

    public void setMpBarData(float value, int valueMax) {
        if (valueMax == 0) {
            valueMax = cachedMaxMana;
        } else {
            cachedMaxMana = valueMax;
        }
        setValueBar(mpBar, (float) Math.floor(value), valueMax, context.getString(R.string.MP_default), ContextCompat.getColor(context, R.color.mpColor), R.drawable.ic_header_magic);
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
        hpBar.setPartyMembers(true);
        mpBar.setPartyMembers(true);
        xpBar.setPartyMembers(true);
    }
}
