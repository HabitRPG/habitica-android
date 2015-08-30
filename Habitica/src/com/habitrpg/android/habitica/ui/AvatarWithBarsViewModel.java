package com.habitrpg.android.habitica.ui;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.ValueBarBinding;
import com.habitrpg.android.habitica.userpicture.UserPicture;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Stats;

/**
 * Created by Negue on 14.06.2015.
 */
public class AvatarWithBarsViewModel {
    ValueBarBinding hpBar;
    ValueBarBinding xpBar;
    ValueBarBinding mpBar;

    ImageView image;
    UserPicture userPicture;

    android.content.res.Resources res;

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


        SetValueBar(hpBar, 50, 50, context.getString(R.string.HP_default), R.color.hpColor);
        SetValueBar(xpBar, 1, 1, context.getString(R.string.XP_default), R.color.xpColor);
        SetValueBar(mpBar, 100, 100, context.getString(R.string.MP_default), R.color.mpColor);
    }

    public void UpdateData(HabitRPGUser user)
    {
        Stats stats = user.getStats();
        
        SetValueBar(hpBar, stats.getHp().floatValue(), stats.getMaxHealth(), context.getString(R.string.HP_default), context.getResources().getColor(R.color.hpColor));
        SetValueBar(xpBar, stats.getExp().floatValue(), stats.getToNextLevel(), context.getString(R.string.XP_default), context.getResources().getColor(R.color.xpColor));
        SetValueBar(mpBar, stats.getMp().floatValue(), stats.getMaxMP(), context.getString(R.string.MP_default), context.getResources().getColor(R.color.mpColor));

        new UserPicture(user, this.context).setPictureOn(image);
    }

    // Layout_Weight don't accepts 0.7/0.3 to have 70% filled instead it shows the 30% , so I had to switch the values
    // but on a 1.0/0.0 which switches to 0.0/1.0 it shows the blank part full size...
    private void SetValueBar(ValueBarBinding valueBar, float value, float valueMax, String description, int color)
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
    }

    @BindingAdapter("app:layout_weight")
    public static void setLayoutWeight(View view, float weight) {
        LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams)view.getLayoutParams();

        Log.d("setLayoutWeight", weight + "");

        layout.weight = weight;

        view.setLayoutParams(layout);
    }

    @BindingAdapter("app:layout_weight_anim")
    public static void setLayoutWeightAnim(View view, float weight) {
        LayoutWeightAnimation anim = new LayoutWeightAnimation(view, weight);
        anim.setDuration(1250);

        view.startAnimation(anim);
    }

    @BindingAdapter("app:rounded_background")
    public static void setRoundedBackground(View view, int color) {
        Drawable drawable = view.getResources().getDrawable(R.drawable.layout_rounded_bg);
        drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        if(Build.VERSION.SDK_INT < 16) {
            view.setBackgroundDrawable(drawable);
        } else {
            view.setBackground(drawable);
        }
    }

    @BindingAdapter("app:rounded_background_int")
    public static void setRoundedBackgroundInt(View view, int color) {
        setRoundedBackground(view, view.getResources().getColor(color));
    }

    public static class LayoutWeightAnimation extends Animation {
        float targetWeight;
        float initializeWeight;
        View view;

        LinearLayout.LayoutParams layoutParams;

        public LayoutWeightAnimation(View view, float targetWeight) {
            this.view = view;
            this.targetWeight = targetWeight;

            layoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
            initializeWeight = layoutParams.weight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            layoutParams.weight = initializeWeight + (targetWeight - initializeWeight) * interpolatedTime;
            
           view.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
