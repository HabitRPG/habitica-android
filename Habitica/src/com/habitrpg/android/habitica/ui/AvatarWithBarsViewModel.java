package com.habitrpg.android.habitica.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
    private UserPicture userPicture;
    private ImageView classIcon;

    private android.content.res.Resources res;

    private Context context;

    public AvatarWithBarsViewModel(Context context, View v){
        this.context = context;

        res = context.getResources();

        if(v == null)
        {
            Log.w("AvatarWithBarsViewModel", "View is null");
            return;
        }

        //binding = DataBindingUtil.bind(v);

        View hpBarView = v.findViewById(R.id.hpBar);

        image = (ImageView) v.findViewById(R.id.IMG_ProfilePicture);
        hpBar = DataBindingUtil.bind(hpBarView);
        xpBar = DataBindingUtil.bind(v.findViewById(R.id.xpBar));
        mpBar = DataBindingUtil.bind(v.findViewById(R.id.mpBar));


        setValueBar(hpBar, 50, 50, context.getString(R.string.HP_default), R.color.hpColor, R.drawable.ic_header_heart);
        setValueBar(xpBar, 1, 1, context.getString(R.string.XP_default), R.color.xpColor, R.drawable.ic_header_exp);
        setValueBar(mpBar, 100, 100, context.getString(R.string.MP_default), R.color.mpColor, R.drawable.ic_header_magic);
    }

    public void updateData(HabitRPGUser user)
    {
        Stats stats = user.getStats();

        setHpBarData(hpBar, stats, context);
        setValueBar(xpBar, stats.getExp().floatValue(), stats.getToNextLevel(), context.getString(R.string.XP_default), context.getResources().getColor(R.color.xpColor), R.drawable.ic_header_exp);
        setValueBar(mpBar, stats.getMp().floatValue(), stats.getMaxMP(), context.getString(R.string.MP_default), context.getResources().getColor(R.color.mpColor), R.drawable.ic_header_magic);
        new UserPicture(user, this.context).setPictureOn(image);


    }

    public static void setHpBarData(ValueBarBinding valueBar, Stats stats, Context ctx)
    {
        int maxHP = stats.getMaxHealth();
        if(maxHP == 0)
        {
            maxHP = 50;
        }

        setValueBar(valueBar, stats.getHp().floatValue(), maxHP, ctx.getString(R.string.HP_default), ctx.getResources().getColor(R.color.hpColor), R.drawable.ic_header_heart);
    }

    // Layout_Weight don't accepts 0.7/0.3 to have 70% filled instead it shows the 30% , so I had to switch the values
    // but on a 1.0/0.0 which switches to 0.0/1.0 it shows the blank part full size...
    private static void setValueBar(ValueBarBinding valueBar, float value, float valueMax, String description, int color, int icon)
    {
        double percent = Math.min(1, value / valueMax);

        if(percent == 1)
        {
            valueBar.setWeightToShow(1);
            valueBar.setWeightToHide(0);
        }
        else
        {
            valueBar.setWeightToShow((float) (1 - percent));
            valueBar.setWeightToHide((float) percent);
        }

        valueBar.setText((int) value + "/" + (int) valueMax);
        valueBar.setDescription(description);
        valueBar.setBarForegroundColor(color);
        valueBar.icHeader.setImageResource(icon);
    }
}
