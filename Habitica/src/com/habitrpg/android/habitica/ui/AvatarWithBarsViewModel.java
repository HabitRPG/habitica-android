package com.habitrpg.android.habitica.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.ValueBarBinding;
import com.habitrpg.android.habitica.userpicture.UserPicture;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Stats;

/**
 * Created by Negue on 14.06.2015.
 */
public class AvatarWithBarsViewModel {
    private ValueBarBinding hpBar;
    private ValueBarBinding xpBar;
    private ValueBarBinding mpBar;

    private ImageView image;

    private android.content.res.Resources res;

    private Context context;

    private TextView lvlText, goldText, silverText, gemsText;

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

        image = (ImageView) v.findViewById(R.id.IMG_ProfilePicture);
        hpBar = DataBindingUtil.bind(hpBarView);
        xpBar = DataBindingUtil.bind(v.findViewById(R.id.xpBar));
        mpBar = DataBindingUtil.bind(v.findViewById(R.id.mpBar));


        setValueBar(hpBar, 50, 50, context.getString(R.string.HP_default), R.color.hpColor, R.drawable.ic_header_heart);
        setValueBar(xpBar, 1, 1, context.getString(R.string.XP_default), R.color.xpColor, R.drawable.ic_header_exp);
        setValueBar(mpBar, 100, 100, context.getString(R.string.MP_default), R.color.mpColor, R.drawable.ic_header_magic);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void updateData(HabitRPGUser user) {
        Stats stats = user.getStats();
        char classShort;
        String userClass = "";
        int gp = (stats.getGp().intValue());
        int sp = (int) ((stats.getGp() - gp) * 100);
        setHpBarData(hpBar, stats, context);
        setValueBar(xpBar, stats.getExp().floatValue(), stats.getToNextLevel(), context.getString(R.string.XP_default), context.getResources().getColor(R.color.xpColor), R.drawable.ic_header_exp);
        setValueBar(mpBar, stats.getMp().floatValue(), stats.getMaxMP(), context.getString(R.string.MP_default), context.getResources().getColor(R.color.mpColor), R.drawable.ic_header_magic);
        new UserPicture(user, this.context).setPictureOn(image);

        if (user.getStats().get_class() != null) {
            userClass += user.getStats().get_class().name();
        }
        lvlText.setText("Lvl" + user.getStats().getLvl() + " " + userClass);
        Drawable drawable;
        switch (stats.get_class()) {
            case warrior:
                drawable = ResourcesCompat.getDrawable(res, R.drawable.ic_header_warrior, null);

                break;
            case rogue:
                drawable = ResourcesCompat.getDrawable(res, R.drawable.ic_header_rogue, null);
                break;
            case wizard:
                drawable = ResourcesCompat.getDrawable(res, R.drawable.ic_header_mage, null);

                break;
            case healer:
                drawable = ResourcesCompat.getDrawable(res, R.drawable.ic_header_healer, null);

                break;
            case base:
            default:
                drawable = ResourcesCompat.getDrawable(res, R.drawable.ic_header_warrior, null);

        }
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                drawable.getMinimumHeight());
        lvlText.setCompoundDrawables(drawable, null, null, null);

//        binding.setClassShort(classShort);

        goldText.setText(gp + "");
        silverText.setText(sp + "");

        Double gems = new Double(user.getBalance() * 4);
        gemsText.setText(gems.intValue() + "");
    }

    public static void setHpBarData(ValueBarBinding valueBar, Stats stats, Context ctx) {
        int maxHP = stats.getMaxHealth();
        if (maxHP == 0) {
            maxHP = 50;
        }

        setValueBar(valueBar, stats.getHp().floatValue(), maxHP, ctx.getString(R.string.HP_default), ctx.getResources().getColor(R.color.hpColor), R.drawable.ic_header_heart);
    }

    // Layout_Weight don't accepts 0.7/0.3 to have 70% filled instead it shows the 30% , so I had to switch the values
    // but on a 1.0/0.0 which switches to 0.0/1.0 it shows the blank part full size...
    private static void setValueBar(ValueBarBinding valueBar, float value, float valueMax, String description, int color, int icon) {
        double percent = Math.min(1, value / valueMax);

        if (percent == 1) {
            valueBar.setWeightToShow(1);
            valueBar.setWeightToHide(0);
        } else {
            valueBar.setWeightToShow((float) percent);
            valueBar.setWeightToHide((float) (1 - percent));
        }

        valueBar.setText((int) value + "/" + (int) valueMax);
        valueBar.setDescription(description);
        valueBar.setBarForegroundColor(color);
        valueBar.icHeader.setImageResource(icon);
    }
}
